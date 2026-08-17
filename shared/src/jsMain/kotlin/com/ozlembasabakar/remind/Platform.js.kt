package com.ozlembasabakar.remind

import web.navigator.navigator

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
}

actual fun getPlatform(): Platform = JsPlatform()

actual val defaultBaseUrl: String = "http://localhost:8080"

actual fun createPlatformHttpClient(): io.ktor.client.HttpClient = io.ktor.client.HttpClient(io.ktor.client.engine.js.Js)