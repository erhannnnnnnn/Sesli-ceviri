package com.example.ytsubtitlespeaker.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class CaptionListResponse(val items: List<CaptionItem> = emptyList())
data class CaptionItem(val id: String, val snippet: CaptionSnippet)
data class CaptionSnippet(val language: String, val name: String = "")

data class TranslationRequest(val q: List<String>, val target: String, val format: String = "text")
data class TranslationResponse(val data: TranslationData)
data class TranslationData(val translations: List<TranslationItem>)
data class TranslationItem(val translatedText: String)

interface YouTubeApi {
    @GET("captions")
    suspend fun listCaptions(
        @Query("part") part: String = "snippet",
        @Query("videoId") videoId: String,
        @Query("key") apiKey: String
    ): Response<CaptionListResponse>

    @GET("captions")
    suspend fun downloadCaption(
        @Query("id") captionId: String,
        @Query("tfmt") format: String = "srt",
        @Query("key") apiKey: String
    ): Response<String>
}

interface TranslationApi {
    @retrofit2.http.POST("language/translate/v2")
    suspend fun translate(
        @Query("key") apiKey: String,
        @retrofit2.http.Body request: TranslationRequest
    ): Response<TranslationResponse>
}
