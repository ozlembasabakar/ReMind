package com.ozlembasabakar.remind.data.audio

class JsAudioPlayer : AudioPlayer {
    override suspend fun playAudio(urlOrAssetPath: String) {
        println("Playing audio on JS target: $urlOrAssetPath")
    }

    override fun stop() {}
}

actual fun createAudioPlayer(): AudioPlayer = JsAudioPlayer()
