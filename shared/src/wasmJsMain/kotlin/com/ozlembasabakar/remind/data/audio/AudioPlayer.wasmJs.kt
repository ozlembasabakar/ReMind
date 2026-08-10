package com.ozlembasabakar.remind.data.audio

class WasmJsAudioPlayer : AudioPlayer {
    override fun stop() {}
}

actual fun createAudioPlayer(): AudioPlayer = WasmJsAudioPlayer()
