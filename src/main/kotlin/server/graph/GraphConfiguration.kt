package com.zealsinger.kotlin.agent.server.graph


import com.alibaba.cloud.ai.graph.KeyStrategy
import com.alibaba.cloud.ai.graph.KeyStrategyFactory
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction
import com.zealsinger.kotlin.agent.server.edge.TestSceneBranchEdge
import com.zealsinger.kotlin.agent.server.nodes.TestSceneRouterNode
import com.zealsinger.kotlin.agent.server.nodes.TestStudyPlanNode
import com.zealsinger.kotlin.agent.server.nodes.TestTravelPlanNode
import com.zealsinger.kotlin.agent.server.nodes.TestWrapUpNode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {
    @Bean
    open fun testBranchStreamingGraph(
        testSceneRouterNode: TestSceneRouterNode,
        testSceneBranchEdge: TestSceneBranchEdge,
        testTravelPlanNode: TestTravelPlanNode,
        testStudyPlanNode: TestStudyPlanNode,
        testWrapUpNode: TestWrapUpNode,
    ): StateGraph {
        val keyStrategyFactory = KeyStrategyFactory {
            mutableMapOf(
                TestGraphSpec.StateKey.INPUT to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.SCENE to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.SCENE_LABEL to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.DRAFT to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.FINAL_OUTPUT to KeyStrategy.REPLACE,
            )
        }

        return StateGraph(keyStrategyFactory)
            .addNode(TestGraphSpec.Node.ROUTE, AsyncNodeAction.node_async(testSceneRouterNode))
            .addNode(TestGraphSpec.Node.TRAVEL_PLAN, AsyncNodeAction.node_async(testTravelPlanNode))
            .addNode(TestGraphSpec.Node.STUDY_PLAN, AsyncNodeAction.node_async(testStudyPlanNode))
            .addNode(TestGraphSpec.Node.WRAP_UP, AsyncNodeAction.node_async(testWrapUpNode))
            .addEdge(StateGraph.START, TestGraphSpec.Node.ROUTE)
            .addConditionalEdges(
                TestGraphSpec.Node.ROUTE,
                AsyncEdgeAction.edge_async(testSceneBranchEdge),
                mapOf(
                    TestGraphSpec.Scene.TRAVEL to TestGraphSpec.Node.TRAVEL_PLAN,
                    TestGraphSpec.Scene.STUDY to TestGraphSpec.Node.STUDY_PLAN,
                ),
            )
            .addEdge(TestGraphSpec.Node.TRAVEL_PLAN, TestGraphSpec.Node.WRAP_UP)
            .addEdge(TestGraphSpec.Node.STUDY_PLAN, TestGraphSpec.Node.WRAP_UP)
            .addEdge(TestGraphSpec.Node.WRAP_UP, StateGraph.END)
    }
}