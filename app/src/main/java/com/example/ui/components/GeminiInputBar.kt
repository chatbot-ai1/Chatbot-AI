package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier
) {
    val isKeyboardOpen = WindowInsets.isImeVisible

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isKeyboardOpen) {
                    Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                } else {
                    Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating pill input container
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sparkle indicator inside pill
                Box(
                    modifier = Modifier.padding(end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GeminiSparkleIcon(size = 20.dp)
                }

                // Text input area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 28.dp, max = 120.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Ask Social AI anything…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Voice / Mic icon
                    AnimatedVisibility(
                        visible = inputText.isEmpty() && !isGenerating,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = {
                                onInputChanged("Tell me an interesting fun fact about space exploration!")
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice prompt shortcut",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Send or Stop button
                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .testTag("stop_generating_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Generating...",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onSendMessage,
                            enabled = inputText.isNotBlank(),
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputText.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = if (inputText.isNotBlank())
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Social AI Disclaimer: only shown when keyboard is not open to avoid pushing pill up
        if (!isKeyboardOpen) {
            Text(
                text = "Social AI may display inaccurate info, including about people, so double-check its responses.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}
