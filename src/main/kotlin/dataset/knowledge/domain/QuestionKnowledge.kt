package com.zealsinger.kotlin.agent.dataset.knowledge.domain

import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
import java.util.*

@Entity
interface QuestionKnowledge {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    val databaseId: String
    val question: String
    val answer: String
}
