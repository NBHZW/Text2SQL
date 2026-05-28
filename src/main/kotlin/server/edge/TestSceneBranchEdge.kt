package com.zealsinger.kotlin.agent.server.edge

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.EdgeAction
import com.zealsinger.kotlin.agent.server.graph.TestGraphSpec
import org.springframework.stereotype.Component

@Component
class TestSceneBranchEdge : EdgeAction{
    // 判断SCENE场景 如果不存在则默认为STUDY场景
    override fun apply(state: OverAllState): String {
        return state.value(TestGraphSpec.StateKey.SCENE, TestGraphSpec.Scene.STUDY)
    }
}