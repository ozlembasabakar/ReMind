package com.ozlembasabakar.remind.data.audio

class JvmAudioPlayer : AudioPlayer {
    override suspend fun speakText(text: String) {
        println("Pronouncing German word on Desktop JVM: '$text'")
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val os = System.getProperty("os.name").lowercase()
                val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                val ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encodedText&tl=de&client=tw-ob"

                if (os.contains("win")) {
                    val tempFile = java.io.File.createTempFile("remind_de_tts_", ".mp3")
                    tempFile.deleteOnExit()
                    val safePath = tempFile.absolutePath.replace("\\", "/")
                    val script = """
                        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;
                        ${'$'}wc = New-Object System.Net.WebClient;
                        ${'$'}wc.Headers.Add('User-Agent', 'Mozilla/5.0');
                        ${'$'}wc.DownloadFile('$ttsUrl', '$safePath');
                        Add-Type -AssemblyName presentationCore;
                        ${'$'}player = New-Object System.Windows.Media.MediaPlayer;
                        ${'$'}player.Open('$safePath');
                        ${'$'}player.Play();
                        Start-Sleep -s 3
                    """.trimIndent().replace("\n", " ")
                    ProcessBuilder("powershell", "-Command", script).start()
                } else if (os.contains("mac")) {
                    ProcessBuilder("say", "-v", "Anna", text).start()
                } else {
                    ProcessBuilder("spd-say", "-l", "de", text).start()
                }
            }.onFailure { ex ->
                println("Desktop TTS exception: ${ex.message}")
            }
        }
    }

    override fun stop() {
        // No-op
    }
}

actual fun createAudioPlayer(): AudioPlayer = JvmAudioPlayer()
