package com.zealsinger.kotlin.agent.controller

import io.a2a.spec.AgentCard
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class A2AController(
    private val agentCard: AgentCard,
)
{
    data class Message(val message: String)

    @GetMapping("/hello")
    fun hello(): Message {
        return Message("ZealSinger")
    }

    @GetMapping(value = ["/a2a"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun agentJson(): AgentCard{
        return agentCard
    }

    @PostMapping(value= ["/a2a/jsonrpc"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE])
    fun handleRequest(@RequestBody body: String): String {
        return body
    }
}
