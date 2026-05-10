
/*
 * A2AConfiguration 配置类
 * 
 * 该类是Spring Boot应用的配置类，用于配置A2A（Agent-to-Agent）服务的核心组件。
 * 主要功能包括：
 * - 定义Agent卡片信息（名称、描述、能力、技能等）
 * - 配置任务存储（InMemoryTaskStore）
 * - 配置推送通知配置存储和发送器
 * - 配置队列管理器（InMemoryQueueManager）
 * - 配置Agent执行超时时间
 * - 配置JSON-RPC处理器
 * 
 * 该配置类使用了Spring的@Configuration注解，所有@Bean方法都会被Spring容器管理。
 */
package com.zealsinger.kotlin.agent.server.a2a

import io.a2a.server.agentexecution.AgentExecutor
import io.a2a.server.events.InMemoryQueueManager
import io.a2a.server.events.QueueManager
import io.a2a.server.requesthandlers.DefaultRequestHandler
import io.a2a.server.tasks.*
import io.a2a.spec.AgentCapabilities
import io.a2a.spec.AgentCard
import io.a2a.spec.AgentInterface
import io.a2a.spec.AgentSkill
import io.a2a.spec.TransportProtocol
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
open class A2AConfiguration {
    /**
     * 创建并配置Agent卡片（AgentCard）
     * 
     * Agent卡片是A2A规范中定义的Agent元数据，包含了Agent的基本信息、能力、技能等。
     * 这个方法配置了一个名为"sqlAgent"的专业SQL生成Agent，支持流式响应、推送通知和状态转换历史。
     * 
     * @return 配置好的AgentCard实例，包含以下关键信息：
     *         - name: "sqlAgent" - Agent的唯一标识名称
     *         - description: "专业的SQL生成Agent" - Agent的功能描述
     *         - defaultInputModes: ["text/plain"] - 支持的输入格式
     *         - defaultOutputModes: ["text/plain"] - 支持的输出格式
     *         - capabilities: 包含streaming、pushNotifications、stateTransitionHistory等能力
     *         - skills: 定义了"sql"技能，用于SQL查询生成
     *         - url: JSON-RPC服务端点地址
     *         - version: "1.0" - Agent版本号
     */
    private val A2A_JSONRPC_URL = "http://localhost:3500/api/a2a/jsonrpc"
    @Bean
    open fun agentCard(): AgentCard {
        return AgentCard.Builder()
            .name("sqlAgent")
            .description("专业的SQL生成Agent")
            .defaultInputModes(listOf("text/plain"))
            .defaultOutputModes(listOf("text/plain"))
            .capabilities(
                AgentCapabilities.Builder()
                    .streaming(true)
                    .pushNotifications(true)
                    .stateTransitionHistory(true)
                    .build()
            )
            .skills(
                listOf(
                    AgentSkill.Builder()
                        .id("sql")
                        .name("sql generator")
                        .description("Generate a SQL query")
                        .tags(listOf("sql", "query"))
                        .build()
                )
            )
            .url(A2A_JSONRPC_URL)
            .additionalInterfaces(listOf(AgentInterface(TransportProtocol.JSONRPC.asString(), A2A_JSONRPC_URL)))
            .version("1.0")
            .build()
    }


    /**
     * 创建内存任务存储（InMemoryTaskStore）
     * 
     * 任务存储用于保存和管理Agent执行的任务信息，包括任务状态、输入输出、执行时间等。
     * 使用内存存储适用于开发和测试环境，生产环境可能需要使用数据库等持久化存储。
     * 
     * @return InMemoryTaskStore实例，提供以下核心功能：
     *         - 任务创建、查询、更新、删除操作
     *         - 任务状态管理（pending、running、completed、failed等）
     *         - 任务历史记录和审计跟踪
     *         - 并发安全的任务操作
     */
    @Bean
    open fun taskStore(): InMemoryTaskStore {
        return InMemoryTaskStore()
    }

    /**
     * 创建推送通知配置存储（InMemoryPushNotificationConfigStore）
     * 
     * 推送通知配置存储用于保存Agent的推送通知配置信息，如通知端点、认证信息、订阅关系等。
     * 使用内存存储适用于开发和测试环境。
     * 
     * @return InMemoryPushNotificationConfigStore实例，提供以下功能：
     *         - 通知配置的CRUD操作
     *         - 通知端点管理
     *         - 认证信息存储
     *         - 订阅关系维护
     */
    @Bean
    open fun pushNotificationConfigStore(): InMemoryPushNotificationConfigStore {
        return InMemoryPushNotificationConfigStore()
    }

    /**
     * 创建推送通知发送器（BasePushNotificationSender）
     * 
     * 推送通知发送器负责向客户端发送任务状态更新等推送通知。
     * 它依赖于推送通知配置存储来获取通知配置信息，并通过HTTP/WebSocket等方式发送通知。
     * 
     * @param pushNotificationConfigStore 推送通知配置存储实例，用于获取通知配置
     * @return BasePushNotificationSender实例，提供以下功能：
     *         - 任务状态变更通知发送
     *         - 实时推送能力
     *         - 通知重试机制
     *         - 通知失败处理
     */
    @Bean
    open fun pushNotificationSender(pushNotificationConfigStore: PushNotificationConfigStore): BasePushNotificationSender {
        return BasePushNotificationSender(pushNotificationConfigStore)
    }


    /**
     * 创建队列管理器（InMemoryQueueManager）
     * 
     * 队列管理器负责管理任务队列，处理任务的入队、出队、状态变更和并发控制。
     * 它依赖于任务存储来保存任务状态信息，并确保任务处理的顺序性和一致性。
     * 
     * @param taskStore 任务存储实例，用于持久化任务状态
     * @return QueueManager实例，提供以下核心功能：
     *         - 任务排队和调度
     *         - 任务优先级管理
     *         - 并发任务处理
     *         - 任务超时处理
     *         - 队列监控和统计
     */
    @Bean
    open fun queueManager(taskStore: InMemoryTaskStore): QueueManager {
        return InMemoryQueueManager(taskStore)
    }


    /**
     * 获取Agent执行超时时间配置
     * 
     * 该配置项定义了阻塞式Agent调用的最大等待时间（秒）。
     * 默认值为30秒，可以通过application.yml中的a2a.blocking.agent.timeout.seconds属性进行配置。
     * 超时时间影响Agent调用的响应性和用户体验。
     * 
     * @param timeout 从配置文件读取的超时时间，默认为30秒
     * @return 超时时间（秒），用于控制Agent执行的最大等待时间
     *         - 较短的超时时间：提高响应性，但可能导致任务被中断
     *         - 较长的超时时间：确保任务完成，但可能影响用户体验
     */
    @Bean
    open fun agentCompletionTimeoutSeconds(
        @Value("\${a2a.blocking.agent.timeout.seconds:30}") timeout: Int
    ): Int {
        return timeout
    }

    /**
     * 创建JSON-RPC处理器（JSONRPCHandler）
     * 
     * JSON-RPC处理器是A2A服务的核心组件，负责处理JSON-RPC协议的请求和响应。
     * 它整合了Agent卡片、Agent执行器、任务存储、队列管理器、推送通知配置存储和发送器等组件，
     * 提供标准化的API接口供客户端调用。
     * 
     * @param agentCard Agent卡片实例，提供Agent元数据信息
     * @param agentExecutor Agent执行器实例，负责实际执行Agent逻辑
     * @param taskStore 任务存储实例，用于管理任务状态
     * @param queueManager 队列管理器实例，用于任务调度和并发控制
     * @param pushNotificationConfigStore 推送通知配置存储实例，用于获取通知配置
     * @param pushNotificationSender 推送通知发送器实例，用于发送任务状态通知
     * @return JSONRPCHandler实例，提供以下核心功能：
     *         - JSON-RPC 2.0协议解析和序列化
     *         - 请求路由和方法分发
     *         - 错误处理和异常转换
     *         - 请求验证和参数校验
     *         - 响应格式化和标准化
     */
    @Bean
    open fun jsonRpcHandler(
        agentCard: AgentCard,
        agentExecutor: AgentExecutor,
        taskStore: TaskStore,
        queueManager: QueueManager,
        pushNotificationConfigStore: PushNotificationConfigStore,
        pushNotificationSender: PushNotificationSender
    ): JSONRPCHandler {
        val pool = Executors.newFixedThreadPool(5);
        val handler = DefaultRequestHandler.create(
            agentExecutor,
            taskStore,
            queueManager,
            pushNotificationConfigStore,
            pushNotificationSender,
            pool
        )
        return JSONRPCHandler(agentCard, handler, pool)
    }

    /*
     * 配置类总结：
     * 
     * 该配置类完整定义了A2A服务所需的全部核心组件：
     * 1. Agent卡片 - 定义Agent的元数据和能力
     * 2. 任务存储 - 管理任务状态和执行历史
     * 3. 推送通知系统 - 实现任务状态的实时推送
     * 4. 队列管理器 - 处理任务队列和并发控制
     * 5. 超时配置 - 控制Agent执行的最大等待时间
     * 6. JSON-RPC处理器 - 提供标准的API接口
     * 
     * 这些组件共同构成了一个完整的A2A服务框架，支持SQL生成Agent的运行。
     */
}