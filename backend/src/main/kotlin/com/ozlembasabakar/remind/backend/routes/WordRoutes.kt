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

fun Route.configureWordRoutes(firestoreService: FirestoreService) {
    route("/health") {
        get {
            call.respond(ApiResponse.success(mapOf("status" to "ok", "service" to "ReMind Backend")))
        }
    }

    route("/api/v1") {
        get("/words") {
            try {
                val words = firestoreService.getAllWords()
                call.respond(ApiResponse.success(words))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Nothing>("FIRESTORE_ERROR", e.message ?: "Unknown error"))
            }
        }

        get("/words/due") {
            try {
                val words = firestoreService.getDueWords()
                call.respond(ApiResponse.success(words))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Nothing>("FIRESTORE_ERROR", e.message ?: "Unknown error"))
            }
        }

        post("/srs/review") {
            try {
                val request = call.receive<SrsReviewRequestDto>()
                val updated = firestoreService.updateSrsStatus(request.wordId, request.rating)
                if (updated != null) {
                    call.respond(ApiResponse.success(updated))
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiResponse.error<Nothing>("NOT_FOUND", "Word with ID ${request.wordId} not found."))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Nothing>("INVALID_REQUEST", e.message ?: "Invalid request body"))
            }
        }
    }
}
