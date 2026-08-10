package com.ozlembasabakar.remind.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

private var isInitialized = false

actual fun initializeFirebase() {
    if (!isInitialized) {
        runCatching {
            val options = FirebaseOptions(
                apiKey = "AIzaSyAT6KGX75dyzlE0dRLuy27SC4hXFcQ_vhY",
                applicationId = "1:265296099829:android:f22d44ef41a68e97bf48bc",
                projectId = "remind-98595",
                storageBucket = "remind-98595.firebasestorage.app"
            )
            Firebase.initialize(options = options)
            isInitialized = true
        }
    }
}
