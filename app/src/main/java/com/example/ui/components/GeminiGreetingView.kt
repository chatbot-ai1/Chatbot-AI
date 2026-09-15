package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiAIStarGradient

data class SuggestionPrompt(
    val title: String,
    val subtitle: String,
    val prompt: String,
    val icon: ImageVector
)

val DEFAULT_SUGGESTIONS = listOf(
    SuggestionPrompt(
        title = "Brainstorm ideas",
        subtitle = "Creative concepts for a mobile side project",
        prompt = "Suggest 5 innovative and practical mobile app ideas that solve everyday problems.",
        icon = Icons.Default.Lightbulb
    ),
    SuggestionPrompt(
        title = "Help me write",
        subtitle = "A thoughtful professional email",
        prompt = "Draft a professional yet warm email following up on a job interview I had yesterday.",
        icon = Icons.Default.Create
    ),
    SuggestionPrompt(
        title = "Explain concepts",
        subtitle = "How quantum computing works",
        prompt = "Explain quantum computing and qubits to someone with no physics background in simple terms.",
        icon = Icons.Default.School
    ),
    SuggestionPrompt(
        title = "Code assistant",
        subtitle = "Kotlin Coroutines Flow example",
        prompt = "Show me an example of Kotlin Flow with retryWhen and catch operators for handling network requests.",
        icon = Icons.Default.Code
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiGreetingView(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Sparkle Icon
        GeminiSparkleIcon(size = 40.dp, animated = true)

        Spacer(modifier = Modifier.height(14.dp))

        // Branding & Greeting Header
        Text(
            text = "Social AI",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                brush = GeminiAIStarGradient
            ),
            fontSize = 36.sp
        )

        Text(
            text = "by Social Info Tech",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "How can I help you today?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Suggestion Cards Header
        Text(
            text = "Try asking about",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Responsive grid of suggestions
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 2
        ) {
            DEFAULT_SUGGESTIONS.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clickable { onSuggestionClick(item.prompt) }
                        .testTag("suggestion_card_$index"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                lineHeight = 15.sp,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
