package com.zealsinger.kotlin.agent

import org.babyfish.jimmer.client.EnableImplicitApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableImplicitApi
@SpringBootApplication
open class DataAgentApplication

fun main(args: Array<String>) {
    runApplication<DataAgentApplication>(*args)
}