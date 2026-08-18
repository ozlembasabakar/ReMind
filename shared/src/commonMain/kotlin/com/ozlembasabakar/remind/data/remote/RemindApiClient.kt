package com.ozlembasabakar.remind.data.remote

import com.ozlembasabakar.remind.createPlatformHttpClient
import com.ozlembasabakar.remind.dto.ApiResponse
import com.ozlembasabakar.remind.dto.SrsReviewRequestDto
import com.ozlembasabakar.remind.dto.WordDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RemindApiClient(
    private val getBaseUrl: () -> String = { ApiConfig.baseUrl },
    private val client: HttpClient = configureHttpClient(createPlatformHttpClient()),
) {

    private fun getCandidates(): List<String> {
        val current = getBaseUrl()
        val defaults = listOf(
            current,
            "http://127.0.0.1:8080",
            "http://10.0.2.2:8080",
            "http://localhost:8080"
        )
        return defaults.distinct()
    }

    suspend fun getAllWords(): List<WordDto> {
        var lastException: Exception? = null
        for (baseUrl in getCandidates()) {
            try {
                val response: ApiResponse<List<WordDto>> =
                    client.get("$baseUrl/api/v1/words").body()
                if (response.success && response.data != null) {
                    ApiConfig.init(baseUrl)
                    return response.data
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException ?: Exception("Failed to fetch words from backend.")
    }

    suspend fun getDueWords(): List<WordDto> {
        var lastException: Exception? = null
        for (baseUrl in getCandidates()) {
            try {
                val response: ApiResponse<List<WordDto>> =
                    client.get("$baseUrl/api/v1/words/due").body()
                if (response.success && response.data != null) {
                    ApiConfig.init(baseUrl)
                    return response.data
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException ?: Exception("Failed to fetch due words from backend.")
    }

    suspend fun postSrsReview(wordId: String, rating: String): WordDto {
        var lastException: Exception? = null
        for (baseUrl in getCandidates()) {
            try {
                val response: ApiResponse<WordDto> = client.post("$baseUrl/api/v1/srs/review") {
                    contentType(ContentType.Application.Json)
                    setBody(SrsReviewRequestDto(wordId = wordId, rating = rating))
                }.body()

                if (response.success && response.data != null) {
                    ApiConfig.init(baseUrl)
                    return response.data
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException ?: Exception("Failed to record SRS review.")
    }

    companion object {
        fun configureHttpClient(baseClient: HttpClient): HttpClient = baseClient.config {
            install(HttpTimeout) {
                requestTimeoutMillis = 4000
                connectTimeoutMillis = 2500
                socketTimeoutMillis = 4000
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
}