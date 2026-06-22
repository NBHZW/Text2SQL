package com.zealsinger.kotlin.agent.dataset.knowledge.repository


import com.zealsinger.kotlin.agent.dataset.knowledge.domain.QuestionKnowledge
import com.zealsinger.kotlin.agent.dataset.knowledge.domain.databaseId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import java.util.*

interface QuestionKnowledgeRepository : KRepository<QuestionKnowledge, UUID>{
    fun findByDatabaseId(databaseId: String): List<QuestionKnowledge>{
        return sql.createQuery(QuestionKnowledge::class){
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }
}