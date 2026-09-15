package com.example.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessage
import com.example.data.local.ChatRepository
import com.example.data.local.ChatSession
import com.example.data.pref.AppPreferences
import com.example.data.pref.ThemeMode
import com.example.data.remote.ApiMessage
import com.example.data.remote.OpenRouterChatRequest
import com.example.data.remote.OpenRouterClient
import com.example.data.remote.supabase.SupabaseClient
import com.example.data.remote.supabase.SupabaseSyncRepository
import com.example.data.remote.supabase.SupabaseSyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = ChatRepository(database.chatDao())
    private val supabaseSync = SupabaseSyncRepository(application, database.chatDao())
    val preferences = AppPreferences(application)

    val supabaseSyncState: StateFlow<SupabaseSyncState> = supabaseSync.syncState

    init {
        if (SupabaseClient.isConfigured()) {
            syncWithSupabase()
        }
    }

    val allSessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSession: StateFlow<ChatSession?> = _currentSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getSession(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val apiKey: StateFlow<String> = preferences.apiKeyFlow
    val selectedModel: StateFlow<String> = preferences.selectedModelFlow
    val themeMode: StateFlow<ThemeMode> = preferences.themeModeFlow

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun selectModel(modelId: String) {
        preferences.setSelectedModel(modelId)
    }

    fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode)
    }

    fun startNewChat() {
        _currentSessionId.value = null
        _inputText.value = ""
        _errorMessage.value = null
    }

    fun selectSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        _inputText.value = ""
        _errorMessage.value = null
    }

    fun syncWithSupabase() {
        viewModelScope.launch {
            supabaseSync.syncAll()
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
            repository.deleteSession(sessionId)
            supabaseSync.deleteRemoteSession(sessionId)
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            val sessions = allSessions.value
            _currentSessionId.value = null
            repository.clearAllHistory()
            sessions.forEach { sess ->
                supabaseSync.deleteRemoteSession(sess.id)
            }
        }
    }

    fun sendMessage(userPrompt: String = _inputText.value) {
        val trimmedPrompt = userPrompt.trim()
        if (trimmedPrompt.isEmpty()) return
        if (_isGenerating.value) return

        val key = apiKey.value.trim()
        if (key.isBlank()) {
            _errorMessage.value = "OpenRouter API key not configured. Developer: Set OPENROUTER_API_KEY in Secrets panel."
            return
        }

        _inputText.value = ""
        _errorMessage.value = null

        viewModelScope.launch {
            _isGenerating.value = true

            try {
                var sessionId = _currentSessionId.value
                val model = selectedModel.value

                // If on Home screen (no active session), create a new session
                if (sessionId == null) {
                    val title = deriveTitle(trimmedPrompt)
                    sessionId = repository.createSession(title, model)
                    _currentSessionId.value = sessionId
                    supabaseSync.uploadSession(
                        ChatSession(id = sessionId, title = title, model = model)
                    )
                }

                // Add user message to DB
                val userMsgId = repository.addMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = trimmedPrompt
                )
                supabaseSync.uploadMessage(
                    ChatMessage(
                        id = userMsgId,
                        sessionId = sessionId,
                        role = "user",
                        content = trimmedPrompt
                    )
                )

                // Load chat history for context
                val history = repository.getMessageHistory(sessionId)
                val contextMessages = history.takeLast(14).map {
                    ApiMessage(role = it.role, content = it.content)
                }

                val systemPrompt = ApiMessage(
                    role = "system",
                    content = "You are Social AI by Social Info Tech, a helpful, intelligent, empathetic, and knowledgeable AI assistant. Provide clear, well-structured, and markdown-friendly responses."
                )

                val requestMessages = mutableListOf<ApiMessage>()
                requestMessages.add(systemPrompt)
                requestMessages.addAll(contextMessages)

                val request = OpenRouterChatRequest(
                    model = model,
                    messages = requestMessages,
                    temperature = 0.7
                )

                val authHeader = if (key.startsWith("Bearer ")) key else "Bearer $key"
                var activeModel = model
                var response = withContext(Dispatchers.IO) {
                    OpenRouterClient.api.createChatCompletion(
                        authorization = authHeader,
                        request = request
                    )
                }

                // If selected model is offline or returns 404 ("No endpoints found"), automatically fallback to openrouter/free
                if (!response.isSuccessful && (response.code() == 404 || response.code() == 503) && activeModel != "openrouter/free") {
                    activeModel = "openrouter/free"
                    preferences.setSelectedModel("openrouter/free")
                    response = withContext(Dispatchers.IO) {
                        OpenRouterClient.api.createChatCompletion(
                            authorization = authHeader,
                            request = request.copy(model = "openrouter/free")
                        )
                    }
                }

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val assistantContent = responseBody?.choices?.firstOrNull()?.message?.content
                    val errorMsg = responseBody?.error?.message

                    val (contentToAdd, isErr) = when {
                        !assistantContent.isNullOrBlank() -> assistantContent to false
                        !errorMsg.isNullOrBlank() -> "API Error: $errorMsg" to true
                        else -> "Received empty response from the AI model." to true
                    }
                    val asstMsgId = repository.addMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = contentToAdd,
                        isError = isErr
                    )
                    supabaseSync.uploadMessage(
                        ChatMessage(
                            id = asstMsgId,
                            sessionId = sessionId,
                            role = "assistant",
                            content = contentToAdd,
                            isError = isErr
                        )
                    )
                } else {
                    val errorBodyStr = response.errorBody()?.string()
                    // Extract clean message from json if available
                    val cleanErrorMsg = if (!errorBodyStr.isNullOrBlank()) {
                        val messageMatch = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(errorBodyStr)
                        messageMatch?.groupValues?.getOrNull(1) ?: errorBodyStr.take(150)
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }

                    val userFriendlyMsg = when (response.code()) {
                        401 -> "Invalid OpenRouter API key. Please verify the key configured in the Secrets panel."
                        402 -> "Free tier rate limit reached or credits depleted. Please wait a moment."
                        404 -> "Model unavailable ($cleanErrorMsg). Switched to OpenRouter Free Router. Please try again."
                        429 -> "Rate limit exceeded. Please wait a moment before sending another message."
                        else -> "Request failed: $cleanErrorMsg. Please try again."
                    }

                    val asstMsgId = repository.addMessage(
                        sessionId = sessionId,
                        role = "assistant",
                        content = userFriendlyMsg,
                        isError = true
                    )
                    supabaseSync.uploadMessage(
                        ChatMessage(
                            id = asstMsgId,
                            sessionId = sessionId,
                            role = "assistant",
                            content = userFriendlyMsg,
                            isError = true
                        )
                    )
                }
            } catch (e: Exception) {
                val errorText = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "No internet connection. Please verify your network and try again."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Request timed out. The model took too long to respond. Please try again."
                    else -> "Connection error: ${e.localizedMessage ?: "Unknown error occurred"}"
                }

                _currentSessionId.value?.let { sessId ->
                    repository.addMessage(
                        sessionId = sessId,
                        role = "assistant",
                        content = errorText,
                        isError = true
                    )
                }
                _errorMessage.value = errorText
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun deriveTitle(prompt: String): String {
        val clean = prompt.replace("\n", " ").trim()
        return if (clean.length <= 32) {
            clean
        } else {
            clean.substring(0, 32).trimEnd() + "…"
        }
    }
}
