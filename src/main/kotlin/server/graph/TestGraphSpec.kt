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

    object ArtifactOutputType {
        const val HUMAN_CONFIRMATION = "HUMAN_CONFIRMATION"
        const val HUMAN_CONFIRMED = "HUMAN_CONFIRMED"
    }

    object ArtifactDataKey {
        const val NEED_CONFIRMATION = "needConfirmation"
        const val CONFIRMED = "confirmed"
    }

    // 前端A2A消息中的metadata里的字段名
    object MessageMetadataKey {
        const val CONFIRMATION_APPROVED = "confirmationApproved"
        const val CONFIRMATION_FEEDBACK = "confirmationFeedback"
    }

    object StateKey {
        const val INPUT = "input" // 前端传入用户输入
        const val SCENE = "scene" // 路由节点判断出的场景，例如是学习场景study还是旅游场景travel
        const val SCENE_LABEL = "sceneLabel" // 场景中文名，用于展示和拼接prompt
        const val DRAFT = "draft" // 分支节点生成的草稿结果
        const val FINAL_OUTPUT = "finalOutput" // 回收的最终输出结果
        const val CONFIRMATION_APPROVED = "confirmationApproved" // 用户是否确认执行
        const val CONFIRMATION_FEEDBACK = "confirmationFeedback" // 确认节点的用户反馈
    }

    object Scene {
        const val TRAVEL = "travel"
        const val STUDY = "study"
    }

}