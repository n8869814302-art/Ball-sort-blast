package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val vRotation: Float
)

@Composable
fun ConfettiOverlay(
    isActive: Boolean,
    onFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFF2A55),
            Color(0xFFFF8C00),
            Color(0xFFFFEA00),
            Color(0xFF00E676),
            Color(0xFF00E5FF),
            Color(0xFF2979FF),
            Color(0xFFD500F9),
            Color(0xFFFF1784)
        )
        List(80) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f, // Start in upper half
                vx = (Random.nextFloat() - 0.5f) * 0.4f,
                vy = Random.nextFloat() * 0.7f + 0.4f,
                size = Random.nextFloat() * 12f + 8f,
                color = colors[Random.nextInt(colors.size)],
                rotation = Random.nextFloat() * 360f,
                vRotation = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        onFinished()
    }

    val t = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val curX = (p.x + p.vx * t) * size.width
            val curY = (p.y + p.vy * t + 0.5f * 9.8f * t * t * 0.1f) * size.height
            val curRot = p.rotation + p.vRotation * t
            val alpha = (1f - t * 0.8f).coerceIn(0f, 1f)

            rotate(curRot, pivot = Offset(curX, curY)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(curX - p.size / 2f, curY - p.size / 2f),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
