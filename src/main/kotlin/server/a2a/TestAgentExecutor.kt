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

import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.agentexecution.RequestContext
import io.a2a.server.events.EventQueue
import io.a2a.server.tasks.TaskUpdater
import io.a2a.spec.TextPart
import org.springframework.stereotype.Component

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
class TestAgentExecutor: AgentExecutor {
    /**
     * 执行Agent任务的核心方法
     * 
     * 该方法处理客户端发送的请求，执行具体的业务逻辑，并通过TaskUpdater更新任务状态。
     * 在A2A架构中，这是Agent执行器最重要的方法，负责实际的业务处理。
     * 
     * @param context 请求上下文，包含客户端请求的完整信息：
     *        - message: 请求消息对象
     *        - parts: 消息内容部分列表
     *        - task: 关联的任务对象
     *        - agentCard: Agent卡片信息
     * @param eventQueue 事件队列，用于异步事件处理和通知
     * 
     * @implSpec 该实现执行以下步骤：
     * 1. 创建TaskUpdater实例用于更新任务状态
     * 2. 从请求上下文中提取文本输入
     * 3. 构建响应文本（添加问候语前缀）
     * 4. 通过addArtifact方法添加输出工件
     * 5. 调用complete()方法标记任务完成
     * 
     * @see TaskUpdater 用于更新任务状态和输出的工具类
     * @see TextPart 文本内容部分的封装类
     */
    override fun execute(
        context: RequestContext?,
        eventQueue: EventQueue?
    ) {
        val taskUpdater = TaskUpdater(context, eventQueue)
        val text = ((context?.message?.parts?.first { it is TextPart }) as TextPart).text
        taskUpdater.addArtifact(
            listOf(TextPart("hello from a2a: $text")),
            "1",
            "TEST_HELLO_NODE",
            mapOf("outputType" to "GRAPH_NODE_STREAMING")
        )
        taskUpdater.complete()
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

    /*
     * TestAgentExecutor 架构总结：
     * 
     * 该测试执行器在A2A架构中扮演以下关键角色：
     * 
     * 1. 请求处理层：接收RequestContext和EventQueue，作为A2A协议的入口点
     * 2. 业务逻辑层：实现具体的Agent功能（文本处理）
     * 3. 状态管理层：通过TaskUpdater与任务存储交互，更新任务状态
     * 4. 输出生成层：创建TextPart输出工件，支持流式响应
     * 5. 生命周期管理：提供execute和cancel方法管理任务生命周期
     * 
     * 该实现遵循A2A规范的核心原则：
     * - 基于事件驱动的异步处理
     * - 任务状态的显式管理
     * - 标准化的输入输出格式
     * - 可扩展的插件架构
     * 
     * 注意：这是一个测试实现，生产环境需要：
     * - 更复杂的业务逻辑
     * - 完整的错误处理
     * - 资源管理和清理
     * - 性能优化和监控
     */
}