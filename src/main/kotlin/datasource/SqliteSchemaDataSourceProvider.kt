package com.zealsinger.kotlin.agent.datasource

import org.sqlite.SQLiteDataSource
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

object SqliteSchemaDataSourceProvider: SchemaDataSourceProvider {
    private val dataSourceCache = ConcurrentHashMap<String, DataSource>()

    override fun get(databaseId: String): DataSource {
        return dataSourceCache.computeIfAbsent(databaseId) { id ->
            SQLiteDataSource().apply {
                url = "jdbc:sqlite:E:/llq/dev/dev_20240627/dev_databases/dev_databases/$id/$id.sqlite"
            }
        }
    }
}