package com.zealsinger.kotlin.agent.dataset.knowledge.repository

import com.zealsinger.kotlin.agent.dataset.knowledge.domain.GlossaryKnowledge
import com.zealsinger.kotlin.agent.dataset.knowledge.domain.databaseId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

interface GlossaryKnowledgeRepository : KRepository<GlossaryKnowledge, UUID> {
    fun findByDatabaseId(databaseId: String): List<GlossaryKnowledge> {
        return sql.createQuery(GlossaryKnowledge::class) {
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }
}