package com.zealsinger.kotlin.agent.server.a2a

import com.alibaba.cloud.ai.graph.CompileConfig
import com.alibaba.cloud.ai.graph.NodeOutput
import com.alibaba.cloud.ai.graph.RunnableConfig
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver
import com.alibaba.cloud.ai.graph.streaming.OutputType
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput
import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.DataPart
import io.a2a.spec.Message
import io.a2a.spec.Task
import io.a2a.spec.TaskState
import io.a2a.spec.TaskStatus
import io.a2a.spec.TextPart
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Component
class TestAgentExecutor(
    private val stateGraph: StateGraph,
) : AgentExecutor {

    private val saver = MemorySaver()

    override fun execute(
        context: RequestContext,
        eventQueue: EventQueue
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val artifactNum = AtomicInteger()
        val compiledGraph = stateGraph.compile(
            CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(saver).build())
                .build()
        )

        fun handleNodeOutput(nodeOutput: NodeOutput) {
            if (nodeOutput is StreamingOutput<*>) {
                when (nodeOutput.outputType) {
                    OutputType.GRAPH_NODE_STREAMING -> {
                        val text = nodeOutput.message()?.text?.takeIf { it.isNotEmpty() } ?: return
                        taskUpdater.addArtifact(
                            listOf(TextPart(text)),
                            artifactNum.incrementAndGet().toString(),
                            nodeOutput.node(),
                            mapOf("outputType" to nodeOutput.outputType)
                        )
                    }

                    OutputType.GRAPH_NODE_FINISHED -> taskUpdater.addArtifact(
                        listOf(DataPart(nodeOutput.state().data())),
                        artifactNum.incrementAndGet().toString(),
                        nodeOutput.node(),
                        mapOf("outputType" to nodeOutput.outputType)
                    )

                    else -> {}
                }
                return
            }

            taskUpdater.addArtifact(
                listOf(DataPart(nodeOutput.state().data())),
                artifactNum.incrementAndGet().toString(),
                nodeOutput.node(),
                mapOf()
            )
        }

        val message = context.message
        val newTask = newTask(message)
        eventQueue.enqueueEvent(newTask)

        val input = ((message.parts.first { it is TextPart }) as TextPart).text
        val runnableConfig = RunnableConfig.builder().threadId(newTask.id).build()
        val initialState = mapOf(
            DataAgentSpec.Graph.StateKey.Input.USER_INPUT to input,
            DataAgentSpec.Graph.StateKey.Input.DATABASE_ID to
                (message.metadata[DataAgentSpec.Graph.StateKey.Input.DATABASE_ID] as? String ?: ""),
            DataAgentSpec.Graph.StateKey.Input.MULTI_TURN_CONTEXT to
                (message.metadata[DataAgentSpec.Graph.StateKey.Input.MULTI_TURN_CONTEXT] as? String ?: "(无)"),
        )

        compiledGraph.stream(initialState, runnableConfig)
            .doOnNext(::handleNodeOutput)
            .doOnComplete(taskUpdater::complete)
            .blockLast()
    }

    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
        return
    }

    private fun newTask(request: Message): Task {
        var contextId = request.contextId
        if (contextId == null || contextId.isEmpty()) {
            contextId = UUID.randomUUID().toString()
        }
        var id = UUID.randomUUID().toString()
        if (request.taskId != null && !request.taskId.isEmpty()) {
            id = request.taskId
        }
        return Task(id, contextId, TaskStatus(TaskState.SUBMITTED), null, listOf(request), null)
    }
}
