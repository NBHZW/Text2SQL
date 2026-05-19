package com.zealsinger.kotlin.agent.server.graph


import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction
import com.zealsinger.kotlin.agent.server.nodes.TestNode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GraphConfiguration {
    @Bean
    open fun testNodeGraph(testNode: TestNode): StateGraph {
        return StateGraph()
            .addNode("TOY_HELLO_NODE", AsyncNodeAction.node_async(testNode))
            .addEdge(StateGraph.START, "TOY_HELLO_NODE")
            .addEdge("TOY_HELLO_NODE", StateGraph.END)
    }
}