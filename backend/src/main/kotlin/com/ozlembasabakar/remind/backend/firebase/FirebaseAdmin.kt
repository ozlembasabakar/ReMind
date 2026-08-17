package com.ozlembasabakar.remind.backend.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import java.io.File
import java.io.FileInputStream

object FirebaseAdmin {

    fun initialize(): Firestore {
        if (FirebaseApp.getApps().isEmpty()) {
            val credentials = try {
                val envCredentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
                val keyFile = if (!envCredentialsPath.isNullOrEmpty()) File(envCredentialsPath) else File("service-account-key.json")
                
                if (keyFile.exists()) {
                    println("Initializing Firebase Admin with service account key: ${keyFile.absolutePath}")
                    GoogleCredentials.fromStream(FileInputStream(keyFile))
                } else {
                    println("Service account key file not found. Falling back to Application Default Credentials.")
                    GoogleCredentials.getApplicationDefault()
                }
            } catch (e: Exception) {
                println("Failed to load custom credentials: ${e.message}. Using default credentials.")
                GoogleCredentials.getApplicationDefault()
            }

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()

            FirebaseApp.initializeApp(options)
            println("Firebase Admin SDK initialized successfully.")
        }
        return FirestoreClient.getFirestore()
    }
}
