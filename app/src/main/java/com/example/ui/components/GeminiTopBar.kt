package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.pref.AVAILABLE_MODELS
import com.example.data.pref.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiTopBar(
    selectedModelId: String,
    themeMode: ThemeMode,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onModelSelect: (String) -> Unit,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    val currentModel = AVAILABLE_MODELS.find { it.id == selectedModelId }
        ?: AVAILABLE_MODELS.first()

    TopAppBar(
        modifier = modifier.testTag("gemini_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("open_sidebar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Chat History Sidebar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        title = {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { modelMenuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("model_selector_pill")
                ) {
                    GeminiSparkleIcon(size = 18.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentModel.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Model",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = modelMenuExpanded,
                    onDismissRequest = { modelMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "Choose AI Model (Free)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    AVAILABLE_MODELS.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = model.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (model.id == selectedModelId) FontWeight.Bold else FontWeight.Normal,
                                            color = if (model.id == selectedModelId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = model.provider,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = model.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            onClick = {
                                onModelSelect(model.id)
                                modelMenuExpanded = false
                            },
                            modifier = Modifier.testTag("model_item_${model.id}")
                        )
                    }
                }
            }
        },
        actions = {
            // New chat button
            IconButton(
                onClick = onNewChatClick,
                modifier = Modifier.testTag("top_bar_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Start New Chat",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Dark/Light Theme toggle
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                val icon = when (themeMode) {
                    ThemeMode.DARK -> Icons.Default.DarkMode
                    ThemeMode.LIGHT -> Icons.Default.LightMode
                    ThemeMode.SYSTEM -> Icons.Default.DarkMode
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle Theme",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}
