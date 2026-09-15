package com.example.data.remote.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseSessionDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "title") val title: String,
    @param:Json(name = "created_at") val createdAt: Long,
    @param:Json(name = "updated_at") val updatedAt: Long,
    @param:Json(name = "model") val model: String
)

@JsonClass(generateAdapter = true)
data class SupabaseMessageDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "session_id") val sessionId: Long,
    @param:Json(name = "role") val role: String,
    @param:Json(name = "content") val content: String,
    @param:Json(name = "timestamp") val timestamp: Long,
    @param:Json(name = "is_error") val isError: Boolean = false
)
