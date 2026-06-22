package com.zealsinger.kotlin.agent.dataset.knowledge.repository


import com.zealsinger.kotlin.agent.dataset.knowledge.domain.QuestionKnowledge
import org.babyfish.jimmer.spring.repository.KRepository
import java.util.*

interface QuestionKnowledgeRepository : KRepository<QuestionKnowledge, UUID>{

}