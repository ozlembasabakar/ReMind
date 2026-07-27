package com.ozlembasabakar.remind.data.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidAudioPlayer : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    override suspend fun playAudio(urlOrAssetPath: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                stop()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(urlOrAssetPath)
                    setOnPreparedListener { mp ->
                        mp.start()
                    }
                    setOnErrorListener { _, what, extra ->
                        println("AndroidAudioPlayer error code: $what, extra: $extra")
                        true
                    }
                    prepareAsync()
                }
            }.onFailure { ex ->
                println("AndroidAudioPlayer exception: ${ex.message}")
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
        }
        mediaPlayer = null
    }
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()
