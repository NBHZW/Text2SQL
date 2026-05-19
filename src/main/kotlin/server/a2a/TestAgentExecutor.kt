/*
 * TestAgentExecutor 测试Agent执行器
 * 
 * 该类是一个简单的测试Agent执行器，用于演示A2A（Agent-to-Agent）框架中Agent执行器的基本实现。
 * 它实现了AgentExecutor接口，提供了一个基础的Agent执行逻辑，主要用于开发和测试目的。
 * 
 * 在A2A架构中，Agent执行器负责：
 * - 接收来自客户端的请求上下文
 * - 执行具体的业务逻辑
 * - 通过TaskUpdater更新任务状态和输出
 * - 管理任务的生命周期（执行、取消、完成）
 * 
 * 该测试执行器实现了最简化的功能：接收文本输入，添加问候语前缀，然后完成任务。
 */

package com.zealsinger.kotlin.agent.server.a2a

import com.alibaba.cloud.ai.graph.NodeOutput
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.streaming.OutputType
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput
import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.DataPart
import io.a2a.spec.TextPart
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 * 测试Agent执行器实现类
 *
 * 该类实现了A2A框架的AgentExecutor接口，提供了一个简单的测试Agent执行逻辑。
 * 主要用于验证A2A框架的基本功能和开发调试。
 *
 * @Component Spring组件注解，使该类被Spring容器管理
 * @see AgentExecutor A2A规范定义的Agent执行器接口
 */
@Component
class TestAgentExecutor(private val stateGraph: StateGraph): AgentExecutor {
    override fun execute(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val artifactNum = AtomicInteger()
        fun handlerNodeOutput(nodeOutput: NodeOutput) {
            if (nodeOutput is StreamingOutput<*>) {
                when (nodeOutput.outputType) {
                    OutputType.GRAPH_NODE_STREAMING -> {
                        val text = nodeOutput.message()?.text?.takeIf { it.isNotEmpty() } ?: return
                        taskUpdater.addArtifact(
                            listOf(TextPart(text)),
                            artifactNum.incrementAndGet().toString(),
                            nodeOutput.node(),
                            mapOf("outputType" to nodeOutput.outputType)
                        )
                    }

                    OutputType.GRAPH_NODE_FINISHED -> taskUpdater.addArtifact(
                        listOf(DataPart(nodeOutput.state().data())),
                        artifactNum.incrementAndGet().toString(),
                        nodeOutput.node(),
                        mapOf("outputType" to nodeOutput.outputType)
                    )

                    else -> {}
                }
            }
        }
        val text = ((context?.message?.parts?.first { it is TextPart }) as TextPart).text
        stateGraph.compile().stream(mapOf("input" to text))
            .doOnNext(::handlerNodeOutput)
            .doOnComplete(taskUpdater::complete)
            .blockLast()
    }

    /**
     * 取消Agent任务的方法
     * 
     * 该方法用于处理客户端请求取消正在执行的任务。在A2A架构中，
     * 取消操作允许客户端中断长时间运行的任务，提高系统的响应性和用户体验。
     * 
     * @param context 请求上下文，包含需要取消的任务信息
     * @param eventQueue 事件队列，用于发送取消通知
     * 
     * @implSpec 该实现目前为空，表示不支持取消操作。在生产环境中，
     *         应该实现真正的取消逻辑，如：
     *         - 中断正在执行的线程
     *         - 清理资源
     *         - 更新任务状态为CANCELLED
     *         - 发送取消通知
     * 
     * @see RequestContext 任务请求上下文
     * @see EventQueue 事件队列用于异步通信
     */
    override fun cancel(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
        return
    }

}
