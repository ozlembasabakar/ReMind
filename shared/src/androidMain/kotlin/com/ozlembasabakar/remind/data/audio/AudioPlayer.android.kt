package com.ozlembasabakar.remind.data.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAudioPlayer : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var tts: android.speech.tts.TextToSpeech? = null

    override suspend fun speakText(text: String) {
        val context = com.ozlembasabakar.remind.AndroidContext.applicationContext
        if (context == null) {
            println("AndroidContext applicationContext is null, cannot speak text")
            return
        }
        withContext(Dispatchers.Main) {
            if (tts == null) {
                tts = android.speech.tts.TextToSpeech(context) { status ->
                    if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                        tts?.language = java.util.Locale.GERMAN
                        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "GermanTTS")
                    }
                }
            } else {
                tts?.language = java.util.Locale.GERMAN
                tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "GermanTTS")
            }
        }
    }

    override fun stop() {
        runCatching {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            tts?.stop()
        }
        mediaPlayer = null
    }
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()
