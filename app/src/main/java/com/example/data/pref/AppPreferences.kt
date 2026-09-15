package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val description: String
)

val AVAILABLE_MODELS = listOf(
    AiModelInfo(
        id = "openrouter/free",
        name = "Auto Free Router (Recommended)",
        provider = "OpenRouter",
        description = "Smart router that always selects an active free model"
    ),
    AiModelInfo(
        id = "google/gemma-4-31b-it:free",
        name = "Google Gemma 4 31B (Free)",
        provider = "Google",
        description = "Free high-capability general & reasoning model"
    ),
    AiModelInfo(
        id = "google/gemma-4-26b-a4b-it:free",
        name = "Google Gemma 4 26B (Free)",
        provider = "Google",
        description = "Free fast, efficient multimodal & chat model"
    ),
    AiModelInfo(
        id = "nvidia/nemotron-3-ultra-550b-a55b:free",
        name = "NVIDIA Nemotron 3 Ultra (Free)",
        provider = "NVIDIA",
        description = "Free 550B advanced reasoning model"
    ),
    AiModelInfo(
        id = "poolside/laguna-xs-2.1:free",
        name = "Laguna XS Coding (Free)",
        provider = "Poolside",
        description = "Free specialized agent for software development"
    ),
    AiModelInfo(
        id = "cohere/north-mini-code:free",
        name = "Cohere North Mini Code (Free)",
        provider = "Cohere",
        description = "Free fast code generation and explanation"
    ),
    AiModelInfo(
        id = "nvidia/nemotron-3.5-lightning:free",
        name = "Nemotron 3.5 Lightning (Free)",
        provider = "NVIDIA",
        description = "Free ultra-fast lightweight conversational model"
    )
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("gemini_chatbot_prefs", Context.MODE_PRIVATE)

    private val _apiKeyFlow = MutableStateFlow(getDeveloperApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _selectedModelFlow = MutableStateFlow(getStoredModel())
    val selectedModelFlow: StateFlow<String> = _selectedModelFlow.asStateFlow()

    private val _themeModeFlow = MutableStateFlow(getStoredThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    private fun getDeveloperApiKey(): String {
        val buildKey = try {
            BuildConfig.OPENROUTER_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "YOUR_OPENROUTER_API_KEY") {
            buildKey.trim()
        } else {
            ""
        }
    }

    private fun getStoredModel(): String {
        val stored = prefs.getString(KEY_MODEL, "openrouter/free") ?: "openrouter/free"
        // If the stored model was an old decommissioned model (like mistral), fallback to openrouter/free
        return if (AVAILABLE_MODELS.any { it.id == stored }) stored else "openrouter/free"
    }

    private fun getStoredThemeMode(): ThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeStr ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setSelectedModel(modelId: String) {
        prefs.edit().putString(KEY_MODEL, modelId).apply()
        _selectedModelFlow.value = modelId
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeModeFlow.value = mode
    }

    companion object {
        private const val KEY_MODEL = "selected_model"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
