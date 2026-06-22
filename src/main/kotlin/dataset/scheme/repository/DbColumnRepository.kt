package com.zealsinger.kotlin.agent.dataset.scheme.repository



import com.zealsinger.kotlin.agent.dataset.scheme.domain.DbColumn
import com.zealsinger.kotlin.agent.dataset.scheme.domain.by
import com.zealsinger.kotlin.agent.dataset.scheme.domain.databaseId
import com.zealsinger.kotlin.agent.dataset.scheme.domain.dbTable
import org.babyfish.jimmer.spring.repository.KRepository
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import java.util.*

interface DbColumnRepository : KRepository<DbColumn, UUID> {
    companion object {
        val FETCHER = newFetcher(DbColumn::class).by {
            allScalarFields()
            dbTable {
                allScalarFields()
            }
        }
    }

    fun findByDatabaseId(databaseId: String): List<DbColumn> {
        return sql.createQuery(DbColumn::class) {
            where(table.dbTable.databaseId.eq(databaseId))
            select(table.fetch(FETCHER))
        }.execute()
    }
}