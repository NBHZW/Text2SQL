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
