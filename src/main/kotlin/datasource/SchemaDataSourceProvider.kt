package com.zealsinger.kotlin.agent.datasource

import javax.sql.DataSource

fun interface SchemaDataSourceProvider{
    fun get(databaseId : String): DataSource
}