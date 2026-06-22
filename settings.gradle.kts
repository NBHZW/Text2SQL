pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "data-agent-backend"
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 接入 AgentHub：用户 Maven 仓库装在 E:/apache-maven-3.8.8/Maven_repo（非默认 ~/.m2）
        // 同时挂上 mavenLocal() 兜底，谁先命中谁工作
        maven { url = uri("file:///E:/apache-maven-3.8.8/Maven_repo") }
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }
}