package com.zealsinger.kotlin.agent.dataset.scheme.domain

import com.zealsinger.kotlin.agent.agent.DataAgentSpec
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator
import org.springframework.ai.document.Document
import java.util.UUID



@Entity
interface DbTable  {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator::class)
    val id: UUID
    @Key
    val name: String
    val description: String

    @Key
    val databaseId: String

    @OneToMany(mappedBy = "dbTable")
    val columns: List<DbColumn>
}

fun DbTable.toDocument(): Document {
    // 确保必要字段已加载，或者在 Repository 层强制 Fetch
    return Document(
        description,
        mapOf(
            DataAgentSpec.Retrieval.DocumentMetadataKey.VECTOR_TYPE to DataAgentSpec.Retrieval.VectorType.TABLE,
            DataAgentSpec.Retrieval.DocumentMetadataKey.DATABASE_ID to databaseId,
            DataAgentSpec.Retrieval.DocumentMetadataKey.TABLE_ID to id
        )
    )
}