package com.ozlembasabakar.remind.domain.usecase

import com.ozlembasabakar.remind.data.audio.AudioPlayer

class PlayAudioUseCase(
    private val audioPlayer: AudioPlayer
) {
    suspend operator fun invoke(url: String?, textToSpeak: String = "") {
        if (!url.isNullOrEmpty()) {
            audioPlayer.playAudio(url)
        } else if (textToSpeak.isNotEmpty()) {
            audioPlayer.speakText(textToSpeak)
        }
    }

    fun stop() {
        audioPlayer.stop()
    }
}
