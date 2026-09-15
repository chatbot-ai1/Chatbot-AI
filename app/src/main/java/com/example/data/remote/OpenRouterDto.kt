package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterChatRequest(
    val model: String,
    val messages: List<ApiMessage>,
    val temperature: Double? = 0.7,
    @Json(name = "max_tokens") val maxTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class ApiMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null,
    val error: ApiError? = null
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int? = null,
    val message: ApiMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int? = null,
    @Json(name = "completion_tokens") val completionTokens: Int? = null,
    @Json(name = "total_tokens") val totalTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val message: String? = null,
    val code: Any? = null
)
