package com.ozlembasabakar.remind

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual val defaultBaseUrl: String = "http://localhost:8080"

actual fun createPlatformHttpClient(): io.ktor.client.HttpClient = io.ktor.client.HttpClient(io.ktor.client.engine.okhttp.OkHttp)