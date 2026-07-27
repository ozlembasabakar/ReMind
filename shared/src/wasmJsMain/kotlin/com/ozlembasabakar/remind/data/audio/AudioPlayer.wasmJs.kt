package com.ozlembasabakar.remind.data.audio

class WasmJsAudioPlayer : AudioPlayer {
    override suspend fun playAudio(urlOrAssetPath: String) {
        println("Playing audio on WasmJs target: $urlOrAssetPath")
    }

    override fun stop() {}
}

actual fun createAudioPlayer(): AudioPlayer = WasmJsAudioPlayer()
