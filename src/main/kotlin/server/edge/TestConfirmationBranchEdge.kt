package com.zealsinger.kotlin.agent.server.edge

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.EdgeAction
import com.zealsinger.kotlin.agent.server.graph.TestGraphSpec
import org.springframework.stereotype.Component

@Component
class TestConfirmationBranchEdge : EdgeAction {
    override fun apply(state: OverAllState): String {
        val approved = state.value(TestGraphSpec.StateKey.CONFIRMATION_APPROVED, false)
        if (!approved) {
            return StateGraph.END
        }
        return state.value(TestGraphSpec.StateKey.SCENE, TestGraphSpec.Scene.STUDY)
    }
}
