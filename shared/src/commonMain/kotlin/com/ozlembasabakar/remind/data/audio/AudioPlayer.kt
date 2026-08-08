package com.ozlembasabakar.remind.data.audio

interface AudioPlayer {
    suspend fun playAudio(urlOrAssetPath: String)
    suspend fun speakText(text: String)
    fun stop()
}

expect fun createAudioPlayer(): AudioPlayer
