package com.ozlembasabakar.remind.data.audio

class JsAudioPlayer : AudioPlayer {
    override suspend fun speakText(text: String) {
        runCatching {
            val utterance = js("new SpeechSynthesisUtterance(text)")
            utterance.lang = "de-DE"
            js("window.speechSynthesis.speak(utterance)")
        }.onFailure { ex ->
            println("Web TTS exception: ${ex.message}")
        }
    }

    override fun stop() {
        runCatching {
            js("window.speechSynthesis.cancel()")
        }
    }
}

actual fun createAudioPlayer(): AudioPlayer = JsAudioPlayer()
