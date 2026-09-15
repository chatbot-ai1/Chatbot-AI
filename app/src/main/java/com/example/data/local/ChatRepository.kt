package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getSession(sessionId: Long): Flow<ChatSession?> = chatDao.getSessionById(sessionId)

    fun getMessages(sessionId: Long): Flow<List<ChatMessage>> = chatDao.getMessagesForSession(sessionId)

    suspend fun createSession(title: String, model: String): Long {
        val session = ChatSession(title = title, model = model)
        return chatDao.insertSession(session)
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        val session = ChatSession(id = sessionId, title = title, updatedAt = System.currentTimeMillis())
        chatDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun clearAllHistory() {
        chatDao.clearAllSessions()
    }

    suspend fun addMessage(sessionId: Long, role: String, content: String, isError: Boolean = false): Long {
        val msg = ChatMessage(
            sessionId = sessionId,
            role = role,
            content = content,
            isError = isError
        )
        val msgId = chatDao.insertMessage(msg)
        chatDao.touchSession(sessionId)
        return msgId
    }

    suspend fun getMessageHistory(sessionId: Long): List<ChatMessage> {
        return chatDao.getMessageListForSession(sessionId)
    }
}
