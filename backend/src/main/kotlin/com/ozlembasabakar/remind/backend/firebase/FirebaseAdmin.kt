package com.ozlembasabakar.remind.backend.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

object FirebaseAdmin {
    private val logger = LoggerFactory.getLogger(FirebaseAdmin::class.java)

    fun initialize(): Firestore {
        if (FirebaseApp.getApps().isEmpty()) {
            val candidateKeys = listOfNotNull(
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS")?.let { File(it) },
                File("service-account-key.json"),
                File("serviceAccountKey.json"),
                File("C:\\Users\\ozlem\\.credentials\\remind-service-account-key.json")
            )
            val keyFile = candidateKeys.firstOrNull { it.exists() }

            val credentials = try {
                if (keyFile != null && keyFile.exists()) {
                    logger.info("Initializing Firebase Admin with service account key: {}", keyFile.absolutePath)
                    GoogleCredentials.fromStream(FileInputStream(keyFile))
                } else {
                    logger.warn("No custom service account key found in candidate paths. Falling back to Application Default Credentials.")
                    GoogleCredentials.getApplicationDefault()
                }
            } catch (e: Exception) {
                logger.error("Failed to load custom credentials: {}. Using default credentials.", e.message)
                GoogleCredentials.getApplicationDefault()
            }

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            FirebaseApp.initializeApp(options)
            logger.info("Firebase Admin SDK initialized successfully.")
        }
        return FirestoreClient.getFirestore()
    }
}
