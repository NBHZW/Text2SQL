plugins {
    kotlin("jvm") version "2.3.0"
    id("com.google.devtools.ksp") version "2.3.0"
    id("org.springframework.boot") version "3.2.5"
    id("tech.argonariod.gradle-plugin-jimmer") version "latest.release"
}
apply(plugin = "io.spring.dependency-management")

group = "com.zealsinger.kotlin.agent"
version = "1.0-SNAPSHOT"

// 指定jimmer版本号
jimmer {
    version = "0.10.6"
}

val KOTLIN_LOGGING_VERSION = "8.0.01"
val A2A_SDK_VERSION = "0.3.2.Final"


// 依赖管理
dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:$KOTLIN_LOGGING_VERSION")
    implementation(platform("org.springframework.ai:spring-ai-bom:1.1.2"))
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("com.alibaba.cloud.ai:spring-ai-alibaba-graph-core:1.1.2.2")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
    implementation("io.github.a2asdk:a2a-java-sdk-transport-jsonrpc:$A2A_SDK_VERSION") // a2a协议
}

// 测试工具管理 默认使用 JUnit5
tasks.test {
    useJUnitPlatform()
}

// kotlin jvm 工具链 指定对应JDK21版本
kotlin {
    jvmToolchain(21)
}