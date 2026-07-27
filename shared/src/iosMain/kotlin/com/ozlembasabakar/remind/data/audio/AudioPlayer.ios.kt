package com.ozlembasabakar.remind.data.audio

import platform.AVFoundation.AVPlayer
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.Foundation.NSURL

class IosAudioPlayer : AudioPlayer {
    private var player: AVPlayer? = null

    override suspend fun playAudio(urlOrAssetPath: String) {
        val nsUrl = NSURL.URLWithString(urlOrAssetPath) ?: return
        stop()
        player = AVPlayer(uRL = nsUrl).apply {
            play()
        }
    }

    override fun stop() {
        player?.pause()
        player = null
    }
}

actual fun createAudioPlayer(): AudioPlayer = IosAudioPlayer()
