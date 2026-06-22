package com.zealsinger.kotlin.agent.dataset.scheme.repository



import com.zealsinger.kotlin.agent.dataset.scheme.domain.DbTable
import com.zealsinger.kotlin.agent.dataset.scheme.domain.databaseId
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq

import java.util.UUID


interface DbTableRepository : KRepository<DbTable, UUID> {

    fun findByDatabaseId(databaseId: String): List<DbTable> {
        return sql.createQuery(DbTable::class) {
            where(table.databaseId eq databaseId)
            select(table)
        }.execute()
    }
}