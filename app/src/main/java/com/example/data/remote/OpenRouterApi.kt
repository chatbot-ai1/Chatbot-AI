package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("api/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://ai.studio",
        @Header("X-Title") title: String = "Social AI by Social Info Tech",
        @Body request: OpenRouterChatRequest
    ): Response<OpenRouterChatResponse>
}
