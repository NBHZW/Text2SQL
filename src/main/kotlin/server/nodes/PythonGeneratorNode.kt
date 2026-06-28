package com.zealsinger.kotlin.agent.server.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import com.zealsinger.kotlin.agent.model.Plan
import com.zealsinger.kotlin.agent.model.Schema
import com.zealsinger.kotlin.agent.prompt.PromptManager
import com.zealsinger.kotlin.agent.util.JsonUtil
import com.zealsinger.kotlin.agent.util.MarkdownParserUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component


private val logger = KotlinLogging.logger {}

@Component
class PythonGeneratorNode(private val chatModel: ChatModel, private val promptManager: PromptManager) : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        logger.info { "apply PythonGeneratorNode" }
        val schemeDto = Schema.fromState(state)
        val result =
            state.value(DataAgentSpec.Graph.StateKey.Execution.SQL_EXECUTION_RESULT, SqlExecuteNode.Result::class.java).orElseThrow()
        val executionStep = Plan.getCurrentStep(state)
        val prompt = promptManager.pythonGeneratorPromptTemplate.render(
            mapOf(
                "python_memory" to "500",
                "python_timeout" to "500",
                "database_schema" to schemeDto.buildSchemePrompt(),
                "sample_input" to JsonUtil.toJson(result.resultSet.data),
                "plan_description" to JsonUtil.toJson(executionStep.toolParameters)
            )
        )
        val pythonCode = ChatClient.create(chatModel)
            .prompt()
            .system(prompt)
            .options(
                OpenAiChatOptions.builder()
                    .extraBody(mapOf("enable_thinking" to false))
                    .build()
            )
            .call()
            .content()
        return mapOf(DataAgentSpec.Graph.StateKey.Execution.PYTHON_GENERATION_RESULT to MarkdownParserUtil.extractRawText(pythonCode))
    }
}