package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionById(sessionId: Long): Flow<ChatSession?>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long

    @Update
    suspend fun updateSession(session: ChatSession)

    @Query("UPDATE chat_sessions SET updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun touchSession(sessionId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM chat_sessions")
    suspend fun clearAllSessions()

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    suspend fun getAllSessionsList(): List<ChatSession>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesList(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ChatSession>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC, id ASC")
    suspend fun getMessageListForSession(sessionId: Long): List<ChatMessage>
}
