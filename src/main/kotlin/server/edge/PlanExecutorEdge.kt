package com.zealsinger.kotlin.agent.server.edge

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph.END
import com.alibaba.cloud.ai.graph.action.EdgeAction
import com.zealsinger.kotlin.agent.agent.DataAgentSpec

class PlanExecutorEdge : EdgeAction {
    override fun apply(state: OverAllState): String {
        val nextNode = state.value(DataAgentSpec.Graph.StateKey.Planning.NEXT_NODE, END)
        if (nextNode == END) {
            return END
        }
        return nextNode
    }
}