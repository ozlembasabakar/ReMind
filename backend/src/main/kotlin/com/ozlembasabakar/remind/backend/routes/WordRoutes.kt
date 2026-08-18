package com.ozlembasabakar.remind.backend.routes

import com.ozlembasabakar.remind.backend.service.FirestoreService
import com.ozlembasabakar.remind.dto.ApiResponse
import com.ozlembasabakar.remind.dto.SrsReviewRequestDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

import kotlinx.serialization.Serializable

@Serializable
data class HealthStatusDto(
    val status: String = "ok",
    val service: String = "ReMind Backend"
)

fun Route.configureWordRoutes(firestoreService: FirestoreService) {
    route("/health") {
        get {
            call.respond<ApiResponse<HealthStatusDto>>(ApiResponse.success(HealthStatusDto()))
        }
    }

    route("/api/v1") {
        get("/words") {
            try {
                val words = firestoreService.getAllWords()
                call.respond<ApiResponse<List<com.ozlembasabakar.remind.dto.WordDto>>>(ApiResponse.success(words))
            } catch (e: Exception) {
                call.respond<ApiResponse<String>>(HttpStatusCode.InternalServerError, ApiResponse.error("FIRESTORE_ERROR", e.message ?: "Unknown error"))
            }
        }

        get("/words/due") {
            try {
                val words = firestoreService.getDueWords()
                call.respond<ApiResponse<List<com.ozlembasabakar.remind.dto.WordDto>>>(ApiResponse.success(words))
            } catch (e: Exception) {
                call.respond<ApiResponse<String>>(HttpStatusCode.InternalServerError, ApiResponse.error("FIRESTORE_ERROR", e.message ?: "Unknown error"))
            }
        }

        post("/srs/review") {
            try {
                val request = call.receive<SrsReviewRequestDto>()
                val updated = firestoreService.updateSrsStatus(request.wordId, request.rating)
                if (updated != null) {
                    call.respond<ApiResponse<com.ozlembasabakar.remind.dto.WordDto>>(ApiResponse.success(updated))
                } else {
                    call.respond<ApiResponse<String>>(HttpStatusCode.NotFound, ApiResponse.error("NOT_FOUND", "Word with ID ${request.wordId} not found."))
                }
            } catch (e: Exception) {
                call.respond<ApiResponse<String>>(HttpStatusCode.BadRequest, ApiResponse.error("INVALID_REQUEST", e.message ?: "Invalid request body"))
            }
        }
    }
}
