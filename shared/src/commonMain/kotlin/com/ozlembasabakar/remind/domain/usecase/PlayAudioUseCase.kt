package com.ozlembasabakar.remind.domain.usecase

import com.ozlembasabakar.remind.data.audio.AudioPlayer

class PlayAudioUseCase(
    private val audioPlayer: AudioPlayer
) {
    suspend operator fun invoke(textToSpeak: String) {
        if (textToSpeak.isNotBlank()) {
            audioPlayer.speakText(textToSpeak)
        }
    }

    fun stop() {
        audioPlayer.stop()
    }
}
