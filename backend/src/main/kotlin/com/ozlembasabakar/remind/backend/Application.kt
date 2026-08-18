package com.ozlembasabakar.remind.backend

import com.ozlembasabakar.remind.backend.config.BackendConfig
import com.ozlembasabakar.remind.backend.firebase.FirebaseAdmin
import com.ozlembasabakar.remind.backend.routes.configureWordRoutes
import com.ozlembasabakar.remind.backend.service.FirestoreService
import com.ozlembasabakar.remind.dto.ApiResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ApplicationExceptionHandler")

fun main() {
    BackendConfig.logConfig()
    embeddedServer(Netty, port = BackendConfig.port, host = BackendConfig.host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        if (BackendConfig.environment == "production") {
            // Restrict origins in production
            allowHost("remind.app", schemes = listOf("https"))
            allowHost("127.0.0.1:8080")
            allowHost("localhost:8080")
        } else {
            anyHost()
        }
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(CallLogging)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled server exception on ${call.request.local.uri}: {}", cause.message, cause)
            call.respond<ApiResponse<String>>(
                status = HttpStatusCode.InternalServerError,
                message = ApiResponse.error(
                    code = "INTERNAL_SERVER_ERROR",
                    message = cause.message ?: "An unexpected error occurred on the server."
                )
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            logger.warn("404 Not Found requested: {}", call.request.local.uri)
            call.respond<ApiResponse<String>>(
                status = status,
                message = ApiResponse.error(
                    code = "NOT_FOUND",
                    message = "The requested resource '${call.request.local.uri}' was not found."
                )
            )
        }

        status(HttpStatusCode.BadRequest) { call, status ->
            logger.warn("400 Bad Request on: {}", call.request.local.uri)
            call.respond<ApiResponse<String>>(
                status = status,
                message = ApiResponse.error(
                    code = "BAD_REQUEST",
                    message = "The request parameters or payload format were invalid."
                )
            )
        }
    }

    val firestore = FirebaseAdmin.initialize()
    val firestoreService = FirestoreService(firestore)

    routing {
        configureWordRoutes(firestoreService)
    }
}
