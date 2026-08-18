import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    application
}

application {
    mainClass.set("com.ozlembasabakar.remind.backend.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":shared")) {
        exclude(group = "com.google.firebase")
        exclude(group = "dev.gitlive")
    }
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.logback)
    implementation(libs.firebase.admin)
    implementation(libs.google.auth.oauth2)
    implementation(libs.kotlinx.coroutinesCore)
    implementation(libs.kotlinx.coroutinesGuava)

    testImplementation(libs.kotlin.test)
}
