package com.example.ytsubtitlespeaker.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SpeechController(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context, this)
    private var initialized = false
    private var paused = false
    private var chunks: List<String> = emptyList()
    private var currentIndex = 0

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
    }

    fun speak(text: String, localeCode: String) {
        if (!initialized) return
        val locale = Locale.forLanguageTag(localeCode)
        tts.language = locale
        chunks = chunkForSpeech(text)
        currentIndex = 0
        paused = false
        speakNext()
    }

    fun pause() {
        if (!initialized) return
        paused = true
        tts.stop()
    }

    fun resume() {
        if (!initialized || !paused) return
        paused = false
        speakNext()
    }

    fun stop() {
        paused = false
        currentIndex = 0
        tts.stop()
    }

    fun release() {
        tts.stop()
        tts.shutdown()
    }

    private fun speakNext() {
        if (paused || currentIndex >= chunks.size) return
        val utteranceId = "chunk_$currentIndex"
        tts.speak(chunks[currentIndex], TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        currentIndex += 1
        if (!paused && currentIndex < chunks.size) {
            tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, "pause_$currentIndex")
            tts.speak(chunks[currentIndex], TextToSpeech.QUEUE_ADD, null, "chunk_$currentIndex")
            currentIndex += 1
        }
    }

    private fun chunkForSpeech(text: String): List<String> {
        val maxLen = 400
        if (text.length <= maxLen) return listOf(text)
        val list = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val end = minOf(i + maxLen, text.length)
            list += text.substring(i, end)
            i = end
        }
        return list
    }
}
