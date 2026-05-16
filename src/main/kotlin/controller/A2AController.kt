package com.zealsinger.kotlin.agent.controller

import io.a2a.common.A2AHeaders
import io.a2a.server.ServerCallContext
import io.a2a.server.auth.UnauthenticatedUser
import io.a2a.server.extensions.A2AExtensions
import io.a2a.spec.*
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler
import io.a2a.util.Utils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.jackson.v2.ImmutableModuleV2
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@RestController
class A2AController(
    private val request: HttpServletRequest,
    private val jsonRpcHandler: JSONRPCHandler,
) {
    @PostConstruct
    fun init() {
        Utils.OBJECT_MAPPER.registerModule(ImmutableModuleV2())
    }

    @GetMapping(value = ["/.well-known/agent-card.json"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun agentJson(): AgentCard {
        return jsonRpcHandler.agentCard
    }

    @ApiIgnore
    @PostMapping(
        value = ["/a2a/jsonrpc"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE]
    )
    fun handleRequest(@RequestBody body: String): Any {
        val method = Utils.OBJECT_MAPPER.readTree(body).path("method").asText()
        val context = serverCallContext()
        return when (method) {
            SendStreamingMessageRequest.METHOD -> stream(
                jsonRpcHandler.onMessageSendStream(
                    Utils.OBJECT_MAPPER.readValue(body, SendStreamingMessageRequest::class.java),
                    context
                )
            )

            TaskResubscriptionRequest.METHOD -> stream(
                jsonRpcHandler.onResubscribeToTask(
                    Utils.OBJECT_MAPPER.readValue(body, TaskResubscriptionRequest::class.java),
                    context
                )
            )

            SendMessageRequest.METHOD -> jsonRpcHandler.onMessageSend(
                Utils.OBJECT_MAPPER.readValue(body, SendMessageRequest::class.java),
                context
            )

            GetTaskRequest.METHOD -> jsonRpcHandler.onGetTask(
                Utils.OBJECT_MAPPER.readValue(body, GetTaskRequest::class.java),
                context
            )

            CancelTaskRequest.METHOD -> jsonRpcHandler.onCancelTask(
                Utils.OBJECT_MAPPER.readValue(body, CancelTaskRequest::class.java),
                context
            )

            SetTaskPushNotificationConfigRequest.METHOD -> jsonRpcHandler.setPushNotificationConfig(
                Utils.OBJECT_MAPPER.readValue(body, SetTaskPushNotificationConfigRequest::class.java),
                context
            )

            GetTaskPushNotificationConfigRequest.METHOD -> jsonRpcHandler.getPushNotificationConfig(
                Utils.OBJECT_MAPPER.readValue(body, GetTaskPushNotificationConfigRequest::class.java),
                context
            )

            ListTaskPushNotificationConfigRequest.METHOD -> jsonRpcHandler.listPushNotificationConfig(
                Utils.OBJECT_MAPPER.readValue(body, ListTaskPushNotificationConfigRequest::class.java),
                context
            )

            DeleteTaskPushNotificationConfigRequest.METHOD -> jsonRpcHandler.deletePushNotificationConfig(
                Utils.OBJECT_MAPPER.readValue(body, DeleteTaskPushNotificationConfigRequest::class.java),
                context
            )

            GetAuthenticatedExtendedCardRequest.METHOD -> jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(
                Utils.OBJECT_MAPPER.readValue(body, GetAuthenticatedExtendedCardRequest::class.java),
                context
            )

            else -> JSONRPCErrorResponse(requestId(body), MethodNotFoundError())
        }
    }

    private fun serverCallContext(): ServerCallContext {
        val extensionHeaders = request.getHeaders(A2AHeaders.X_A2A_EXTENSIONS).toList()
        val requestedExtensions = A2AExtensions.getRequestedExtensions(extensionHeaders)
        return ServerCallContext(UnauthenticatedUser.INSTANCE, mutableMapOf(), requestedExtensions)
    }

    private fun stream(publisher: Flow.Publisher<SendStreamingMessageResponse>): SseEmitter {
        val emitter = SseEmitter(TimeUnit.MINUTES.toMillis(5))
        publisher.subscribe(object : Flow.Subscriber<SendStreamingMessageResponse> {
            private var subscription: Flow.Subscription? = null

            override fun onSubscribe(subscription: Flow.Subscription) {
                this.subscription = subscription
                subscription.request(1)
            }

            override fun onNext(item: SendStreamingMessageResponse) {
                try {
                    emitter.send(SseEmitter.event().data(item))
                    subscription?.request(1)
                } catch (ex: Exception) {
                    subscription?.cancel()
                    emitter.completeWithError(ex)
                }
            }

            override fun onError(throwable: Throwable) {
                log.error(throwable) { "A2A stream failed" }
                emitter.completeWithError(throwable)
            }

            override fun onComplete() {
                emitter.complete()
            }
        })
        return emitter
    }

    private fun requestId(body: String): Any? {
        return runCatching {
            val id = Utils.OBJECT_MAPPER.readTree(body).path("id")
            when {
                id.isMissingNode || id.isNull -> null
                id.isNumber -> id.numberValue()
                else -> id.asText()
            }
        }.getOrNull()
    }
}
