package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.pref.ThemeMode
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.GeminiGreetingView
import com.example.ui.components.GeminiInputBar
import com.example.ui.components.GeminiTopBar
import com.example.ui.components.GeneratingIndicator
import com.example.ui.components.SidebarDrawerContent
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val supabaseSyncState by viewModel.supabaseSyncState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(currentMessages.size, isGenerating) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    // Show error in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarDrawerContent(
                sessions = allSessions,
                currentSessionId = currentSessionId,
                themeMode = themeMode,
                supabaseSyncState = supabaseSyncState,
                onSyncSupabase = { viewModel.syncWithSupabase() },
                onSelectSession = { id ->
                    viewModel.selectSession(id)
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = { id ->
                    viewModel.deleteSession(id)
                },
                onClearAllSessions = {
                    viewModel.clearAllSessions()
                    scope.launch { drawerState.close() }
                },
                onNewChat = {
                    viewModel.startNewChat()
                    scope.launch { drawerState.close() }
                },
                onChangeThemeMode = { mode ->
                    viewModel.setThemeMode(mode)
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("chat_scaffold"),
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GeminiTopBar(
                    selectedModelId = selectedModel,
                    themeMode = themeMode,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onNewChatClick = {
                        viewModel.startNewChat()
                    },
                    onModelSelect = { modelId ->
                        viewModel.selectModel(modelId)
                    },
                    onToggleTheme = {
                        val nextMode = when (themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.SYSTEM
                        }
                        viewModel.setThemeMode(nextMode)
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .imePadding()
            ) {
                // Content area: Greeting or Chat list
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (currentSessionId == null && currentMessages.isEmpty()) {
                        // Gemini Home screen greeting & suggestions
                        GeminiGreetingView(
                            onSuggestionClick = { prompt ->
                                viewModel.sendMessage(prompt)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Chat Conversation Messages
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("chat_messages_list")
                        ) {
                            items(currentMessages, key = { it.id }) { message ->
                                ChatMessageBubble(message = message)
                            }

                            if (isGenerating) {
                                item {
                                    GeneratingIndicator()
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Gemini pill input bar
                GeminiInputBar(
                    inputText = inputText,
                    onInputChanged = { viewModel.setInputText(it) },
                    onSendMessage = { viewModel.sendMessage() },
                    isGenerating = isGenerating
                )
            }
        }
    }
}
