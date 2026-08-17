package com.ozlembasabakar.remind.data.remote

import com.ozlembasabakar.remind.createPlatformHttpClient
import com.ozlembasabakar.remind.defaultBaseUrl
import com.ozlembasabakar.remind.dto.ApiResponse
import com.ozlembasabakar.remind.dto.SrsReviewRequestDto
import com.ozlembasabakar.remind.dto.WordDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
    private val baseUrl: String = defaultBaseUrl,
    private val client: HttpClient = configureHttpClient(createPlatformHttpClient()),
) {

    suspend fun getAllWords(): List<WordDto> {
        val response: ApiResponse<List<WordDto>> = client.get("$baseUrl/api/v1/words").body()
        if (response.success && response.data != null) {
            return response.data
        } else {
            throw Exception(response.error?.message ?: "Failed to fetch words from backend.")
        }
    }

    suspend fun getDueWords(): List<WordDto> {
        val response: ApiResponse<List<WordDto>> = client.get("$baseUrl/api/v1/words/due").body()
        if (response.success && response.data != null) {
            return response.data
        } else {
            throw Exception(response.error?.message ?: "Failed to fetch due words from backend.")
        }
    }

    suspend fun postSrsReview(wordId: String, rating: String): WordDto {
        val response: ApiResponse<WordDto> = client.post("$baseUrl/api/v1/srs/review") {
            contentType(ContentType.Application.Json)
            setBody(SrsReviewRequestDto(wordId = wordId, rating = rating))
        }.body()

        if (response.success && response.data != null) {
            return response.data
        } else {
            throw Exception(response.error?.message ?: "Failed to record SRS review.")
        }
    }

    companion object {
        fun configureHttpClient(baseClient: HttpClient): HttpClient = baseClient.config {
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