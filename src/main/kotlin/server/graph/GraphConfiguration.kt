package com.zealsinger.kotlin.agent.server.graph


import com.alibaba.cloud.ai.graph.KeyStrategy
import com.alibaba.cloud.ai.graph.KeyStrategyFactory
import com.alibaba.cloud.ai.graph.OverAllState
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.StateGraph.END
import com.alibaba.cloud.ai.graph.StateGraph.START
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async
import com.alibaba.cloud.ai.graph.serializer.StateSerializer
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.SpringAIJacksonStateSerializer
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import com.zealsinger.kotlin.agent.server.edge.FeasibilityAssessmentEdge
import com.zealsinger.kotlin.agent.server.nodes.EvidenceRecallNode
import com.zealsinger.kotlin.agent.server.nodes.FeasibilityAssessmentNode
import com.zealsinger.kotlin.agent.server.nodes.HumanFeedbackNode
import com.zealsinger.kotlin.agent.server.nodes.PlanExecuteNode
import com.zealsinger.kotlin.agent.server.nodes.PlannerNode
import com.zealsinger.kotlin.agent.server.nodes.SchemeReCallNode
import com.zealsinger.kotlin.agent.server.nodes.TableRelationNode
import org.babyfish.jimmer.jackson.v2.ImmutableModuleV2
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {

    @Bean
    open fun serializer(): SpringAIJacksonStateSerializer {
        val serializer =
            SpringAIJacksonStateSerializer { OverAllState(it) }
        serializer.objectMapper().registerModules(JavaTimeModule())
        serializer.objectMapper().registerModules(ImmutableModuleV2())
        return serializer
    }

    @Bean
    open fun graph(
        evidenceRecallNode: EvidenceRecallNode,
        schemeReCallNode: SchemeReCallNode,
        tableRelationNode: TableRelationNode,
        feasibilityAssessmentNode: FeasibilityAssessmentNode,
        plannerNode: PlannerNode,
        humanFeedbackNode: HumanFeedbackNode,
        planExecuteNode: PlanExecuteNode,
        serializer: StateSerializer
    ): StateGraph {
        val keyStrategyFactory = KeyStrategyFactory {
            val map = mutableMapOf<String, KeyStrategy>()
            map[DataAgentSpec.Graph.StateKey.Input.USER_INPUT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Input.DATABASE_ID] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Input.MULTI_TURN_CONTEXT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.REWRITE_QUERY] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.EVIDENCE] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.TABLE_SCHEMA] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.COLUMN_SCHEMA] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Recall.TABLE_RELATION] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.PLAN] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.REPAIR_COUNT] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.NEXT_NODE] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.CURRENT_STEP] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.Planning.EXECUTION_OUTPUT] = ReplaceStrategy()

            map[DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_APPROVED] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.HumanReview.CONFIRMATION_FEEDBACK] = ReplaceStrategy()
            map[DataAgentSpec.Graph.StateKey.HumanReview.NEXT_NODE] = ReplaceStrategy()

            map[DataAgentSpec.Graph.StateKey.Execution.FEASIBILITY_RESULT] = ReplaceStrategy()
            map
        }
        return StateGraph(DataAgentSpec.GRAPH_NAME, keyStrategyFactory, serializer)
            .addNode(DataAgentSpec.Graph.Node.EVIDENCE_RECALL, node_async(evidenceRecallNode))
            .addNode(DataAgentSpec.Graph.Node.SCHEMA_RECALL, node_async(schemeReCallNode))
            .addNode(DataAgentSpec.Graph.Node.TABLE_RELATION, node_async(tableRelationNode))
            .addNode(DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT, node_async(feasibilityAssessmentNode))
            .addNode(DataAgentSpec.Graph.Node.PLANNER, node_async(plannerNode))
            .addNode(DataAgentSpec.Graph.Node.HUMAN_FEEDBACK, node_async(humanFeedbackNode))
            .addNode(DataAgentSpec.Graph.Node.PLAN_EXECUTION, node_async(planExecuteNode))
            .addEdge(START, DataAgentSpec.Graph.Node.EVIDENCE_RECALL)
            .addEdge(DataAgentSpec.Graph.Node.EVIDENCE_RECALL, DataAgentSpec.Graph.Node.SCHEMA_RECALL)
            .addEdge(DataAgentSpec.Graph.Node.SCHEMA_RECALL, DataAgentSpec.Graph.Node.TABLE_RELATION)
            .addEdge(DataAgentSpec.Graph.Node.TABLE_RELATION, DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT)
            .addConditionalEdges(
                DataAgentSpec.Graph.Node.FEASIBILITY_ASSESSMENT,
                edge_async(FeasibilityAssessmentEdge()),
                mapOf(
                    DataAgentSpec.Graph.Node.PLANNER to DataAgentSpec.Graph.Node.PLANNER,
                    END to END,
                )
            )
            .addEdge(DataAgentSpec.Graph.Node.PLANNER, DataAgentSpec.Graph.Node.HUMAN_FEEDBACK)
            .addConditionalEdges(
                DataAgentSpec.Graph.Node.HUMAN_FEEDBACK, edge_async(HumanFeedbackEdge()),
                mapOf(
                    END to END,
                    DataAgentSpec.Graph.Node.PLAN_EXECUTION to DataAgentSpec.Graph.Node.PLAN_EXECUTION,
                    DataAgentSpec.Graph.Node.PLANNER to DataAgentSpec.Graph.Node.PLANNER
                )
            ).addEdge(DataAgentSpec.Graph.Node.PLAN_EXECUTION, END)

    }

    /*@Bean
    open fun testBranchStreamingGraph(
        testSceneRouterNode: TestSceneRouterNode,
        testTravelPlanNode: TestTravelPlanNode,
        testStudyPlanNode: TestStudyPlanNode,
        testWrapUpNode: TestWrapUpNode,
        testConfirmationBranchEdge: TestConfirmationBranchEdge,
        testResumeNode: TestResumeNode,
    ): StateGraph {
        val keyStrategyFactory = KeyStrategyFactory {
            mutableMapOf(
                TestGraphSpec.StateKey.INPUT to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.SCENE to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.SCENE_LABEL to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.DRAFT to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.FINAL_OUTPUT to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.CONFIRMATION_APPROVED to KeyStrategy.REPLACE,
                TestGraphSpec.StateKey.CONFIRMATION_FEEDBACK to KeyStrategy.REPLACE,
            )
        }

        return StateGraph(keyStrategyFactory)
            .addNode(TestGraphSpec.Node.ROUTE, AsyncNodeAction.node_async(testSceneRouterNode))
            .addNode(TestGraphSpec.Node.CONFIRM, AsyncNodeAction.node_async(testResumeNode))
            .addNode(TestGraphSpec.Node.TRAVEL_PLAN, AsyncNodeAction.node_async(testTravelPlanNode))
            .addNode(TestGraphSpec.Node.STUDY_PLAN, AsyncNodeAction.node_async(testStudyPlanNode))
            .addNode(TestGraphSpec.Node.WRAP_UP, AsyncNodeAction.node_async(testWrapUpNode))
            .addEdge(StateGraph.START, TestGraphSpec.Node.ROUTE)
            .addEdge(TestGraphSpec.Node.ROUTE, TestGraphSpec.Node.CONFIRM)
            .addConditionalEdges(
                TestGraphSpec.Node.CONFIRM,
                AsyncEdgeAction.edge_async(testConfirmationBranchEdge),
                mapOf(
                    TestGraphSpec.Scene.TRAVEL to TestGraphSpec.Node.TRAVEL_PLAN,
                    TestGraphSpec.Scene.STUDY to TestGraphSpec.Node.STUDY_PLAN,
                    StateGraph.END to StateGraph.END,
                ),
            )
            .addEdge(TestGraphSpec.Node.TRAVEL_PLAN, TestGraphSpec.Node.WRAP_UP)
            .addEdge(TestGraphSpec.Node.STUDY_PLAN, TestGraphSpec.Node.WRAP_UP)
            .addEdge(TestGraphSpec.Node.WRAP_UP, StateGraph.END)
    }*/
}
