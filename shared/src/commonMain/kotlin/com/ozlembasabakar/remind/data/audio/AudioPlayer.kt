package com.ozlembasabakar.remind.data.audio

interface AudioPlayer {
    suspend fun playAudio(urlOrAssetPath: String)
    fun stop()
}

expect fun createAudioPlayer(): AudioPlayer
