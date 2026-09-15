package com.example.data.remote.supabase

import android.content.Context
import android.util.Log
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessage
import com.example.data.local.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class SupabaseSyncState {
    object Idle : SupabaseSyncState()
    object Syncing : SupabaseSyncState()
    data class Synced(val lastSyncTimestamp: Long, val messageCount: Int, val sessionCount: Int) : SupabaseSyncState()
    data class Error(val message: String) : SupabaseSyncState()
    object NotConfigured : SupabaseSyncState()
}

class SupabaseSyncRepository(
    context: Context,
    private val chatDao: ChatDao
) {
    private val prefs = context.getSharedPreferences("supabase_sync_prefs", Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow<SupabaseSyncState>(
        if (!SupabaseClient.isConfigured()) SupabaseSyncState.NotConfigured
        else {
            val lastTime = prefs.getLong(KEY_LAST_SYNC, 0L)
            if (lastTime > 0) SupabaseSyncState.Synced(lastTime, 0, 0)
            else SupabaseSyncState.Idle
        }
    )
    val syncState: StateFlow<SupabaseSyncState> = _syncState.asStateFlow()

    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured()) {
            _syncState.value = SupabaseSyncState.NotConfigured
            return@withContext Result.failure(IllegalStateException("Supabase credentials are not configured."))
        }

        val api = SupabaseClient.getApi() ?: run {
            _syncState.value = SupabaseSyncState.NotConfigured
            return@withContext Result.failure(IllegalStateException("Failed to initialize Supabase API."))
        }

        val apiKey = SupabaseClient.getSupabaseAnonKey()
        val authHeader = "Bearer $apiKey"

        _syncState.value = SupabaseSyncState.Syncing

        try {
            // 1. Get all local data
            val localSessions = chatDao.getAllSessionsList()
            val localMessages = chatDao.getAllMessagesList()

            // 2. Push local data to Supabase (upsert)
            if (localSessions.isNotEmpty()) {
                val sessionDtos = localSessions.map {
                    SupabaseSessionDto(
                        id = it.id,
                        title = it.title,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        model = it.model
                    )
                }
                val pushSessionsRes = api.upsertSessions(apiKey = apiKey, authorization = authHeader, sessions = sessionDtos)
                if (!pushSessionsRes.isSuccessful) {
                    val err = "Failed to upload sessions to Supabase (HTTP ${pushSessionsRes.code()})"
                    Log.e("SupabaseSync", err)
                    _syncState.value = SupabaseSyncState.Error(err)
                    return@withContext Result.failure(Exception(err))
                }
            }

            if (localMessages.isNotEmpty()) {
                val messageDtos = localMessages.map {
                    SupabaseMessageDto(
                        id = it.id,
                        sessionId = it.sessionId,
                        role = it.role,
                        content = it.content,
                        timestamp = it.timestamp,
                        isError = it.isError
                    )
                }
                val pushMessagesRes = api.upsertMessages(apiKey = apiKey, authorization = authHeader, messages = messageDtos)
                if (!pushMessagesRes.isSuccessful) {
                    val err = "Failed to upload messages to Supabase (HTTP ${pushMessagesRes.code()})"
                    Log.e("SupabaseSync", err)
                    _syncState.value = SupabaseSyncState.Error(err)
                    return@withContext Result.failure(Exception(err))
                }
            }

            // 3. Pull remote sessions & messages from Supabase
            val remoteSessionsRes = api.getSessions(apiKey = apiKey, authorization = authHeader)
            if (remoteSessionsRes.isSuccessful) {
                val remoteSessions = remoteSessionsRes.body().orEmpty()
                if (remoteSessions.isNotEmpty()) {
                    val entities = remoteSessions.map {
                        ChatSession(
                            id = it.id,
                            title = it.title,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                            model = it.model
                        )
                    }
                    chatDao.insertSessions(entities)
                }
            }

            val remoteMessagesRes = api.getAllMessages(apiKey = apiKey, authorization = authHeader)
            if (remoteMessagesRes.isSuccessful) {
                val remoteMessages = remoteMessagesRes.body().orEmpty()
                if (remoteMessages.isNotEmpty()) {
                    val entities = remoteMessages.map {
                        ChatMessage(
                            id = it.id,
                            sessionId = it.sessionId,
                            role = it.role,
                            content = it.content,
                            timestamp = it.timestamp,
                            isError = it.isError
                        )
                    }
                    chatDao.insertMessages(entities)
                }
            }

            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_LAST_SYNC, now).apply()
            val totalSessions = chatDao.getAllSessionsList().size
            val totalMessages = chatDao.getAllMessagesList().size
            _syncState.value = SupabaseSyncState.Synced(now, totalMessages, totalSessions)
            Result.success(Unit)
        } catch (e: Exception) {
            val errMsg = "Sync error: ${e.localizedMessage ?: "Unknown error"}"
            Log.e("SupabaseSync", errMsg, e)
            _syncState.value = SupabaseSyncState.Error(errMsg)
            Result.failure(e)
        }
    }

    suspend fun uploadSession(session: ChatSession) = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured()) return@withContext
        val api = SupabaseClient.getApi() ?: return@withContext
        val apiKey = SupabaseClient.getSupabaseAnonKey()
        try {
            api.upsertSessions(
                apiKey = apiKey,
                authorization = "Bearer $apiKey",
                sessions = listOf(
                    SupabaseSessionDto(
                        id = session.id,
                        title = session.title,
                        createdAt = session.createdAt,
                        updatedAt = session.updatedAt,
                        model = session.model
                    )
                )
            )
        } catch (e: Exception) {
            Log.w("SupabaseSync", "Error uploading session to Supabase", e)
        }
    }

    suspend fun uploadMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured()) return@withContext
        val api = SupabaseClient.getApi() ?: return@withContext
        val apiKey = SupabaseClient.getSupabaseAnonKey()
        try {
            api.upsertMessages(
                apiKey = apiKey,
                authorization = "Bearer $apiKey",
                messages = listOf(
                    SupabaseMessageDto(
                        id = message.id,
                        sessionId = message.sessionId,
                        role = message.role,
                        content = message.content,
                        timestamp = message.timestamp,
                        isError = message.isError
                    )
                )
            )
        } catch (e: Exception) {
            Log.w("SupabaseSync", "Error uploading message to Supabase", e)
        }
    }

    suspend fun deleteRemoteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        if (!SupabaseClient.isConfigured()) return@withContext
        val api = SupabaseClient.getApi() ?: return@withContext
        val apiKey = SupabaseClient.getSupabaseAnonKey()
        try {
            api.deleteMessagesForSession(apiKey = apiKey, authorization = "Bearer $apiKey", sessionIdFilter = "eq.$sessionId")
            api.deleteSession(apiKey = apiKey, authorization = "Bearer $apiKey", idFilter = "eq.$sessionId")
        } catch (e: Exception) {
            Log.w("SupabaseSync", "Error deleting session from Supabase", e)
        }
    }

    companion object {
        private const val KEY_LAST_SYNC = "key_last_sync"
    }
}
