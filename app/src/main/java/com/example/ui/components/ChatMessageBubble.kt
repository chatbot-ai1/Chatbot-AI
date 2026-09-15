package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(if (isUser) "user_message_row" else "assistant_message_row"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Assistant Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                GeminiSparkleIcon(size = 20.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = when {
                    message.isError -> MaterialTheme.colorScheme.errorContainer
                    isUser -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                border = if (!isUser && !message.isError) {
                    androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                } else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    FormattedMessageText(
                        content = message.content,
                        isError = message.isError,
                        textColor = when {
                            message.isError -> MaterialTheme.colorScheme.onErrorContainer
                            isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            // Message action footer (timestamp + copy button for assistant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )

                if (!isUser && !message.isError) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            copyToClipboard(context, message.content)
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("copy_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(12.dp))
            // User Avatar
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FormattedMessageText(
    content: String,
    isError: Boolean,
    textColor: Color
) {
    // Basic Markdown formatting for bold and code blocks
    val annotated = remember(content, isError, textColor) {
        buildAnnotatedString {
            val lines = content.split("\n")
            var inCodeBlock = false

            for (i in lines.indices) {
                val line = lines[i]
                if (line.trim().startsWith("```")) {
                    inCodeBlock = !inCodeBlock
                    continue
                }

                if (inCodeBlock) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            background = Color.Black.copy(alpha = 0.15f)
                        )
                    ) {
                        append(line)
                    }
                } else {
                    parseInlineMarkdown(line, textColor)
                }

                if (i < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 22.sp,
            letterSpacing = 0.15.sp
        )
    )
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(
    text: String,
    baseColor: Color
) {
    var index = 0
    val length = text.length

    while (index < length) {
        val boldStart = text.indexOf("**", index)
        val codeStart = text.indexOf("`", index)

        val nextSpecial = when {
            boldStart != -1 && codeStart != -1 -> minOf(boldStart, codeStart)
            boldStart != -1 -> boldStart
            codeStart != -1 -> codeStart
            else -> -1
        }

        if (nextSpecial == -1) {
            withStyle(SpanStyle(color = baseColor)) {
                append(text.substring(index))
            }
            break
        }

        if (nextSpecial > index) {
            withStyle(SpanStyle(color = baseColor)) {
                append(text.substring(index, nextSpecial))
            }
        }

        if (nextSpecial == boldStart) {
            val boldEnd = text.indexOf("**", boldStart + 2)
            if (boldEnd != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                    append(text.substring(boldStart + 2, boldEnd))
                }
                index = boldEnd + 2
            } else {
                withStyle(SpanStyle(color = baseColor)) {
                    append("**")
                }
                index = boldStart + 2
            }
        } else {
            val codeEnd = text.indexOf("`", codeStart + 1)
            if (codeEnd != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color.Black.copy(alpha = 0.1f),
                        fontSize = 13.sp
                    )
                ) {
                    append(text.substring(codeStart + 1, codeEnd))
                }
                index = codeEnd + 1
            } else {
                withStyle(SpanStyle(color = baseColor)) {
                    append("`")
                }
                index = codeStart + 1
            }
        }
    }
}

@Composable
fun GeneratingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("generating_indicator"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            GeminiSparkleIcon(size = 20.dp, animated = true)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PulsingDot(delay = 0)
                PulsingDot(delay = 200)
                PulsingDot(delay = 400)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Social AI is thinking…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PulsingDot(delay: Int) {
    val anim = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = anim.value))
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}
