package com.zealsinger.kotlin.agent.server.edge

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.EdgeAction
import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import com.alibaba.cloud.ai.graph.StateGraph.END


private val logger = KotlinLogging.logger {}

class FeasibilityAssessmentEdge: EdgeAction {
    override fun apply(state: OverAllState): String {
        val output = state.value(DataAgentSpec.Graph.StateKey.Execution.FEASIBILITY_RESULT, "")
        if (output.contains("【需求类型】：《数据分析》")) {
            logger.info { "[FeasibilityAssessmentNodeEdge]需求类型为数据分析，进入PlannerNode节点" }
            return DataAgentSpec.Graph.Node.PLANNER
        } else {
            logger.info { "[FeasibilityAssessmentNodeEdge]需求类型非数据分析，返回END节点" }
            return END
        }
    }
}