package com.zealsinger.kotlin.agent.server.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import com.zealsinger.kotlin.agent.datasource.ResultSetBuilder
import com.zealsinger.kotlin.agent.datasource.SqliteSchemaDataSourceProvider
import com.zealsinger.kotlin.agent.model.DisplaySpec
import com.zealsinger.kotlin.agent.model.Plan
import com.zealsinger.kotlin.agent.model.SqlResultSet
import com.zealsinger.kotlin.agent.util.JsonUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.collections.mutableMapOf

private val logger = KotlinLogging.logger {}

@Component
class SqlExecuteNode() : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        val currentStep = state.value(DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP, 1)
        val sql = state.value(DataAgentSpec.Graph.StateKey.Execution.SQL_GENERATION_RESULT, "")
        val databaseId = state.value(DataAgentSpec.Graph.StateKey.Input.DATABASE_ID, "")
        val resultSetWrapper = SqliteSchemaDataSourceProvider.get(databaseId).connection.use {
            ResultSetBuilder.buildFrom(it.createStatement().executeQuery(sql))
        }
        logger.info {
            "sql execute $resultSetWrapper"
        }
        val displaySpec = buildDisplaySpec(resultSetWrapper)
        logger.info { "display spec: $displaySpec" }
        Plan.getCurrentStep(state).toolParameters.sqlQuery = sql
        return mapOf(
            DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP to currentStep + 1,
            DataAgentSpec.Graph.StateKey.Execution.SQL_EXECUTION_RESULT to Result(
                resultSet = resultSetWrapper,
                display = displaySpec,
            ),
            DataAgentSpec.Graph.StateKey.Planning.EXECUTION_OUTPUT to
                    mutableMapOf("step_$currentStep" to JsonUtil.toJson(resultSetWrapper))

        )
    }

    private fun buildDisplaySpec(resultSetWrapper: SqlResultSet): DisplaySpec {
        if (resultSetWrapper.column.isEmpty()) {
            return DisplaySpec(
                type = "table",
                title = "SQL已生成，等待外部执行",
                x = null,
                y = emptyList()
            )
        }
        val xAxis = resultSetWrapper.column.firstOrNull()
        val yAxis = resultSetWrapper.column.drop(1)
        return DisplaySpec(
            type = "table",
            title = "SQL执行结果",
            x = xAxis,
            y = yAxis
        )
    }

    data class Result(
        val resultSet: SqlResultSet,
        val display: DisplaySpec
    )

}