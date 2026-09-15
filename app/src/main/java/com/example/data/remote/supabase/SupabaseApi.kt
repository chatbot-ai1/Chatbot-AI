package com.example.data.remote.supabase

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/chat_sessions")
    suspend fun getSessions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "updated_at.desc"
    ): Response<List<SupabaseSessionDto>>

    @POST("rest/v1/chat_sessions")
    suspend fun upsertSessions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body sessions: List<SupabaseSessionDto>
    ): Response<ResponseBody>

    @DELETE("rest/v1/chat_sessions")
    suspend fun deleteSession(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") idFilter: String
    ): Response<ResponseBody>

    @DELETE("rest/v1/chat_sessions")
    suspend fun deleteAllSessions(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("id") filter: String = "gte.0"
    ): Response<ResponseBody>

    @GET("rest/v1/chat_messages")
    suspend fun getAllMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "timestamp.asc"
    ): Response<List<SupabaseMessageDto>>

    @GET("rest/v1/chat_messages")
    suspend fun getMessagesForSession(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("session_id") sessionIdFilter: String,
        @Query("select") select: String = "*",
        @Query("order") order: String = "timestamp.asc"
    ): Response<List<SupabaseMessageDto>>

    @POST("rest/v1/chat_messages")
    suspend fun upsertMessages(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body messages: List<SupabaseMessageDto>
    ): Response<ResponseBody>

    @DELETE("rest/v1/chat_messages")
    suspend fun deleteMessagesForSession(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("session_id") sessionIdFilter: String
    ): Response<ResponseBody>
}
