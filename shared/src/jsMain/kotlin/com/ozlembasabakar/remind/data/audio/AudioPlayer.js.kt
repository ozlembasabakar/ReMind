package com.ozlembasabakar.remind.data.audio

import co.touchlab.kermit.Logger

class JsAudioPlayer : AudioPlayer {
    override suspend fun speakText(text: String) {
        runCatching {
            val utterance = js("new SpeechSynthesisUtterance(text)")
            utterance.lang = "de-DE"
            js("window.speechSynthesis.speak(utterance)")
        }.onFailure { ex ->
            Logger.withTag("JsAudioPlayer")
                .e(ex) { "Web TTS exception" }
        }
    }

    override fun stop() {
        runCatching {
            js("window.speechSynthesis.cancel()")
        }
    }
}

actual fun createAudioPlayer(): AudioPlayer = JsAudioPlayer()
