package com.ozlembasabakar.remind

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect val defaultBaseUrl: String
expect fun createPlatformHttpClient(): io.ktor.client.HttpClient