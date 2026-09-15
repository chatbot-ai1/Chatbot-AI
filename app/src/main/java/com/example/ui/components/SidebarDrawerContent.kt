package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSession
import com.example.data.pref.ThemeMode
import com.example.data.remote.supabase.SupabaseSyncState

@Composable
fun SidebarDrawerContent(
    sessions: List<ChatSession>,
    currentSessionId: Long?,
    themeMode: ThemeMode,
    supabaseSyncState: SupabaseSyncState,
    onSyncSupabase: () -> Unit,
    onSelectSession: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onClearAllSessions: () -> Unit,
    onNewChat: () -> Unit,
    onChangeThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .statusBarsPadding()
            .testTag("sidebar_drawer"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // App Branding & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                GeminiSparkleIcon(size = 28.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Social AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "by Social Info Tech",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "+ New chat" Button
            Button(
                onClick = onNewChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sidebar_new_chat_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "New chat",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (sessions.isNotEmpty()) {
                    IconButton(
                        onClick = onClearAllSessions,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("clear_all_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear all history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat sessions list
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "No chats",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No chats yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Your conversation history will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    else
                                        androidx.compose.ui.graphics.Color.Transparent
                                )
                                .clickable { onSelectSession(session.id) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("session_item_${session.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Chat",
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = session.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_session_button_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete chat",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Supabase Cloud Sync Card
            SupabaseSyncCard(
                syncState = supabaseSyncState,
                onSyncNow = onSyncSupabase
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Theme Mode Controls
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeModeItem(
                    label = "System",
                    icon = Icons.Default.SettingsBrightness,
                    isSelected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onChangeThemeMode(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
                ThemeModeItem(
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    isSelected = themeMode == ThemeMode.LIGHT,
                    onClick = { onChangeThemeMode(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeModeItem(
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    isSelected = themeMode == ThemeMode.DARK,
                    onClick = { onChangeThemeMode(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeModeItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.surface
                else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        }
    }
}
