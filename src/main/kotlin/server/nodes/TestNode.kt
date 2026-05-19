package com.zealsinger.kotlin.agent.server.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class TestNode(private val chatModel: ChatModel) : NodeAction{
    override fun apply(state: OverAllState): Map<String, Any> {
        val input = state.value("input", "")
        val flux = ChatClient.create(chatModel)
            .prompt()
            .user(input)
            .options(OpenAiChatOptions.builder().extraBody(mapOf("enable_thinking" to false)).build())
            .stream()
            .chatResponse()
        return mapOf("output" to flux)
    }
}