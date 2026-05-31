package com.zealsinger.kotlin.agent.server.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import org.springframework.stereotype.Component

//
@Component
class TestResumeNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        return emptyMap()
    }
}