package com.example.ytsubtitlespeaker.data.model

data class CaptionTrack(
    val id: String,
    val language: String,
    val name: String
)

data class TranscriptResult(
    val originalText: String,
    val translatedText: String
)

enum class TargetLanguage(val code: String, val label: String) {
    TURKISH("tr", "Türkçe"),
    ENGLISH("en", "English"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español")
}
