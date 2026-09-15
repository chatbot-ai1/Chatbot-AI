package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GeminiSparkleIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val currentScale = if (animated) scale else 1.0f

        val brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4285F4),
                Color(0xFF9B72CF),
                Color(0xFFD96570)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )

        // 4-pointed Gemini star path
        val path = Path().apply {
            val r = (w / 2f) * currentScale
            moveTo(cx, cy - r)
            quadraticTo(cx, cy, cx + r, cy)
            quadraticTo(cx, cy, cx, cy + r)
            quadraticTo(cx, cy, cx - r, cy)
            quadraticTo(cx, cy, cx, cy - r)
            close()
        }

        drawPath(path = path, brush = brush)
    }
}
