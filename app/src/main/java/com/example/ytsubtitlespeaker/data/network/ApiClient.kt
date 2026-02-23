package com.example.ytsubtitlespeaker.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val youtubeApi: YouTubeApi = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YouTubeApi::class.java)

    val translationApi: TranslationApi = Retrofit.Builder()
        .baseUrl("https://translation.googleapis.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TranslationApi::class.java)
}
