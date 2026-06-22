package com.zealsinger.kotlin.agent.agent

object DataAgentSpec {
    const val GRAPH_NAME = "data-agent-main-graph"

    object Retrieval {
        // 文档元数据键
        object DocumentMetadataKey {
            const val TABLE_ID = "tableId"
            const val COLUMN_ID = "columnId"
            const val KNOWLEDGE_ID = "knowledgeId"
            const val VECTOR_TYPE = "vectorType"
            const val DATABASE_ID = "databaseId"
            const val BUSINESS_TERM_ID = "businessTermId"
        }

        // 向量类型 用于标识向量的来源
        object VectorType {
            const val QUESTION_KNOWLEDGE = "questionKnowledge"
            const val GLOSSARY_KNOWLEDGE = "glossaryKnowledge"
            const val COLUMN = "column"
            const val TABLE = "table"
        }
    }

}