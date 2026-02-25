plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

group = "com.dailycommit"
version = "1.0.0"

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    mavenCentral()
    google()
}

dependencies {
    // 使用 IntelliJ 自带的 Kotlin stdlib，避免冲突
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")

    // 使用 IntelliJ 自带的协程库，避免冲突
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // 序列化库
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Ktor 依赖 - 排除所有可能冲突的依赖
    implementation("io.ktor:ktor-client-core:2.3.7") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("io.ktor:ktor-client-java:2.3.7") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    // 使用 IntelliJ 自带的 slf4j
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

intellij {
    version.set("2023.1.5")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("Git4Idea"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("253.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
