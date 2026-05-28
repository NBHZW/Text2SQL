package com.zealsinger.kotlin.agent.server.nodes

import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.action.NodeAction
import com.zealsinger.kotlin.agent.server.graph.TestGraphSpec
import org.springframework.stereotype.Component

// 路由节点
@Component
class TestSceneRouterNode : NodeAction {
    override fun apply(state: OverAllState): Map<String, Any> {
        // 获取前端输入
        val input = state.value(TestGraphSpec.StateKey.INPUT,"")
        // 定义旅行关键词
        val travelKeywords = listOf("旅游", "旅行", "出游", "攻略", "景点", "酒店", "美食", "周末去哪")
        // 判断场景
        val scene = if (travelKeywords.any { input.contains(it) }) TestGraphSpec.Scene.TRAVEL else TestGraphSpec.Scene.STUDY
        // 定义场景标签
        val sceneLabel = if (scene == TestGraphSpec.Scene.TRAVEL) "旅行攻略" else "学习计划"
        // 返回结果
        return mapOf(
            TestGraphSpec.StateKey.SCENE to scene,
            TestGraphSpec.StateKey.SCENE_LABEL to sceneLabel,
        )

    }
}