package com.zealsinger.kotlin.agent.dataset.knowledge.domain

import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
import java.util.UUID

@Entity
interface GlossaryKnowledge  {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID // 业务知识唯一ID 目前采用UUID
    val databaseId: String // 所属数据库
    val term: String // 业务名词 目前不使用 预留字段 默认为空字符串
    val description: String // 业务解释
    val synonyms: String? // 同义词 目前也没使用 预留字段
}
