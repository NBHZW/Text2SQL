package com.zealsinger.kotlin.agent.server.graph

object TestGraphSpec {
    const val NAME = "test-branch-streaming-graph"

    object Node {
        const val ROUTE = "ROUTE_NODE"
        const val CONFIRM = "CONFIRM_NODE"
        const val TRAVEL_PLAN = "TRAVEL_PLAN_NODE"
        const val STUDY_PLAN = "STUDY_PLAN_NODE"
        const val WRAP_UP = "WRAP_UP_NODE"
    }

    object StateKey {
        const val INPUT = "input" // 前端传入用户输入
        const val SCENE = "scene" // 路由节点判断出的场景，例如是学习场景study还是旅游场景travel
        const val SCENE_LABEL = "sceneLabel" // 场景中文名，用于展示和拼接prompt
        const val DRAFT = "draft" // 分支节点生成的草稿结果
        const val FINAL_OUTPUT = "finalOutput" // 回收的最终输出结果
    }

    object Scene {
        const val TRAVEL = "travel"
        const val STUDY = "study"
    }

}