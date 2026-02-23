package com.example.ytsubtitlespeaker.data.repository

import com.example.ytsubtitlespeaker.data.model.CaptionTrack
import com.example.ytsubtitlespeaker.data.model.TranscriptResult
import com.example.ytsubtitlespeaker.data.network.TranslationRequest
import com.example.ytsubtitlespeaker.data.network.YouTubeApi
import com.example.ytsubtitlespeaker.data.network.TranslationApi

class SubtitleRepository(
    private val youTubeApi: YouTubeApi,
    private val translationApi: TranslationApi,
    private val youtubeApiKey: String,
    private val translateApiKey: String
) {

    suspend fun listCaptionTracks(videoId: String): Result<List<CaptionTrack>> = runCatching {
        val response = youTubeApi.listCaptions(videoId = videoId, apiKey = youtubeApiKey)
        if (!response.isSuccessful) {
            error("Altyazı listesi alınamadı: ${response.code()} ${response.message()}")
        }
        response.body()?.items.orEmpty().map {
            CaptionTrack(id = it.id, language = it.snippet.language, name = it.snippet.name.ifBlank { "Unnamed" })
        }
    }

    suspend fun fetchAndTranslateCaption(captionId: String, targetLanguage: String): Result<TranscriptResult> = runCatching {
        val downloadResponse = youTubeApi.downloadCaption(captionId = captionId, apiKey = youtubeApiKey)
        if (!downloadResponse.isSuccessful) {
            error("Seçilen altyazı indirilemedi. Bu track için erişim olmayabilir veya API yetkisi eksik olabilir.")
        }

        val rawSrt = downloadResponse.body().orEmpty()
        if (rawSrt.isBlank()) {
            error("Altyazı içeriği boş döndü.")
        }

        val plainText = srtToPlainText(rawSrt)
        val sourceChunks = chunkText(plainText, 450)

        val translatedParts = mutableListOf<String>()
        for (chunk in sourceChunks) {
            val translationResponse = translationApi.translate(
                apiKey = translateApiKey,
                request = TranslationRequest(q = listOf(chunk), target = targetLanguage)
            )
            if (!translationResponse.isSuccessful) {
                error("Çeviri başarısız oldu: ${translationResponse.code()} ${translationResponse.message()}")
            }
            val translated = translationResponse.body()?.data?.translations?.firstOrNull()?.translatedText
                ?: error("Çeviri sonucu boş döndü.")
            translatedParts += translated
        }

        TranscriptResult(originalText = plainText, translatedText = translatedParts.joinToString("\n"))
    }

    private fun srtToPlainText(srt: String): String {
        return srt
            .lines()
            .filterNot { it.matches(Regex("^\\d+$")) }
            .filterNot { it.contains("-->") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }

    private fun chunkText(text: String, maxLen: Int): List<String> {
        if (text.length <= maxLen) return listOf(text)
        val chunks = mutableListOf<String>()
        var cursor = 0
        while (cursor < text.length) {
            val end = minOf(cursor + maxLen, text.length)
            chunks += text.substring(cursor, end)
            cursor = end
        }
        return chunks
    }
}
