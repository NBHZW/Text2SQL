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

import com.alibaba.cloud.ai.graph.CompileConfig
import com.alibaba.cloud.ai.graph.NodeOutput
import com.alibaba.cloud.ai.graph.RunnableConfig
import com.alibaba.cloud.ai.graph.StateGraph
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver
import com.alibaba.cloud.ai.graph.streaming.OutputType
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput
import com.zealsinger.kotlin.agent.server.graph.TestGraphSpec
import com.zealsinger.kotlin.agent.server.graph.TestGraphSpec.MessageMetadataKey.CONFIRMATION_APPROVED
import com.zealsinger.kotlin.agent.server.graph.TestGraphSpec.MessageMetadataKey.CONFIRMATION_FEEDBACK
import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskStore
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.DataPart
import io.a2a.spec.Message
import io.a2a.spec.Task
import io.a2a.spec.TaskState
import io.a2a.spec.TaskStatus
import io.a2a.spec.TextPart
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Component
class TestAgentExecutor(
    private val stateGraph: StateGraph,
    private val taskStore: TaskStore
): AgentExecutor {
    // 创建内存检查点器
    private val saver = MemorySaver()

    override fun execute(
        context: RequestContext,
        eventQueue: EventQueue
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val artifactNum = AtomicInteger()
        // 使用检查点器编译图
        val compiledGraph = stateGraph.compile(
            CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(saver).build())
                .interruptBefore(TestGraphSpec.Node.CONFIRM)
                .build()
        )
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
                return
            }
            taskUpdater.addArtifact(
                listOf(DataPart(nodeOutput.state().data())),
                artifactNum.incrementAndGet().toString(),
                nodeOutput.node(),
                mapOf()
            )
        }
        val message = context.message
        // 判断任务是否存在
        val existingTask = message.taskId?.let(taskStore::get)
        if (existingTask != null) {
            // 任务存在，继续执行
            val runnableConfig = RunnableConfig.builder().threadId(existingTask.id).build()
            // 获取前端输入 默认为不通过
            val approved = message.metadata[CONFIRMATION_APPROVED] as? Boolean ?: false
            // 获取前端输入 默认为空字符串
            val feedback = message.metadata[CONFIRMATION_FEEDBACK] as? String ?: ""
            val resumedConfig = compiledGraph.updateState(
                runnableConfig,
                mapOf(
                    TestGraphSpec.StateKey.CONFIRMATION_APPROVED to approved,
                    TestGraphSpec.StateKey.CONFIRMATION_FEEDBACK to feedback,
                )
            )
            compiledGraph.stream(null,resumedConfig)
                .doOnNext(::handlerNodeOutput)
                .doOnComplete(taskUpdater::complete)
                .blockLast()
            return
        }
        val newTask = newTask(message)
        eventQueue?.enqueueEvent(newTask)

        val input = ((message.parts.first { it is TextPart }) as TextPart).text
        val runnableConfig = RunnableConfig.builder().threadId(newTask.id).build()

        compiledGraph.stream(mapOf(TestGraphSpec.StateKey.INPUT to input), runnableConfig)
            .doOnNext(::handlerNodeOutput)
            .doOnComplete {
                val stateSnapshot = compiledGraph.getState(runnableConfig)
                if (stateSnapshot.next() != TestGraphSpec.Node.CONFIRM) {
                    taskUpdater.complete()
                    return@doOnComplete
                }

                val stateData = LinkedHashMap(stateSnapshot.state().data())
                taskUpdater.addArtifact(
                    listOf(DataPart(
                        mapOf(
                            TestGraphSpec.StateKey.SCENE to stateData[TestGraphSpec.StateKey.SCENE],
                            TestGraphSpec.StateKey.SCENE_LABEL to stateData[TestGraphSpec.StateKey.SCENE_LABEL],
                            TestGraphSpec.ArtifactDataKey.NEED_CONFIRMATION to true,
                        )
                    )),
                    artifactNum.incrementAndGet().toString(),
                    TestGraphSpec.Node.CONFIRM,
                    mapOf("outputType" to TestGraphSpec.ArtifactOutputType.HUMAN_CONFIRMATION)
                )

                taskUpdater.requiresInput(
                    taskUpdater.newAgentMessage(
                        listOf(TextPart("已判断当前场景为${stateData[TestGraphSpec.StateKey.SCENE_LABEL]}，请确认是否继续执行。")),
                        mapOf(
                            TestGraphSpec.StateKey.SCENE to stateData[TestGraphSpec.StateKey.SCENE],
                            TestGraphSpec.StateKey.SCENE_LABEL to stateData[TestGraphSpec.StateKey.SCENE_LABEL],
                        )
                    )
                )
            }
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

    private fun newTask(request: Message): Task {
        var contextId = request.contextId
        if (contextId == null || contextId.isEmpty()) {
            contextId = UUID.randomUUID().toString()
        }
        var id = UUID.randomUUID().toString()
        if (request.taskId != null && !request.taskId.isEmpty()) {
            id = request.taskId
        }
        return Task(id, contextId, TaskStatus(TaskState.SUBMITTED), null, listOf(request), null)
    }

}
