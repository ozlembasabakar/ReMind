package com.ozlembasabakar.remind.data.audio

class JvmAudioPlayer : AudioPlayer {
    override suspend fun playAudio(urlOrAssetPath: String) {
        // Desktop / JVM fallback player simulation
        println("Playing audio on JVM target: $urlOrAssetPath")
    }

    override fun stop() {
        // No-op
    }
}

actual fun createAudioPlayer(): AudioPlayer = JvmAudioPlayer()
