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

// 依赖管理
dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
}

// 测试工具管理 默认使用 JUnit5
tasks.test {
    useJUnitPlatform()
}

// kotlin jvm 工具链 指定对应JDK21版本
kotlin {
    jvmToolchain(21)
}