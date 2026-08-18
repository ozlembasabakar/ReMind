package com.ozlembasabakar.remind.backend.config

import org.slf4j.LoggerFactory

object BackendConfig {
    private val logger = LoggerFactory.getLogger("BackendConfig")

    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host: String = System.getenv("HOST") ?: "0.0.0.0"
    val environment: String = System.getenv("APP_ENV") ?: "development"

    val serviceAccountKeyPath: String? = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        ?: System.getenv("FIREBASE_KEY_PATH")

    fun logConfig() {
        logger.info("=========================================")
        logger.info("Initializing ReMind Backend Environment:")
        logger.info("  Environment : {}", environment)
        logger.info("  Host        : {}", host)
        logger.info("  Port        : {}", port)
        logger.info("  Credentials : {}", serviceAccountKeyPath ?: "Auto-detecting candidate paths")
        logger.info("=========================================")
    }
}
