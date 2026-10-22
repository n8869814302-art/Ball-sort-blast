package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BallPalette
import com.example.model.BallSkin

@Composable
fun BallComposable(
    colorId: Int,
    skin: BallSkin,
    size: Dp = 46.dp,
    isLifted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ballColor = BallPalette.getColor(colorId)

    val infiniteTransition = rememberInfiniteTransition(label = "ball_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = if (isLifted) -4f else 0f,
        targetValue = if (isLifted) 4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_anim"
    )

    val currentOffset = if (isLifted) hoverOffset.dp else 0.dp

    Box(
        modifier = modifier
            .size(size)
            .offset(y = currentOffset)
            .testTag("ball_$colorId"),
        contentAlignment = Alignment.Center
    ) {
        // Draw 3D Shaded Ball
        Canvas(modifier = Modifier.size(size)) {
            val radius = this.size.minDimension / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Outer glow if lifted
            if (isLifted) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ballColor.glow.copy(alpha = 0.6f), Color.Transparent),
                        center = center,
                        radius = radius * 1.4f
                    ),
                    radius = radius * 1.35f,
                    center = center
                )
            }

            // Base 3D Sphere Radial Gradient with offset light source
            val lightSource = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
            val sphereBrush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.9f),
                    0.25f to ballColor.highlight,
                    0.65f to ballColor.primary,
                    1.0f to ballColor.shadow
                ),
                center = lightSource,
                radius = radius * 1.3f
            )

            drawCircle(
                brush = sphereBrush,
                radius = radius,
                center = center
            )

            // Outer crisp rim stroke
            drawCircle(
                color = ballColor.shadow.copy(alpha = 0.5f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Top-left Specular Glass Glint
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.35f),
                    radius = radius * 0.45f
                ),
                radius = radius * 0.35f,
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.35f)
            )
        }

        // Overlay Glyphs based on selected BallSkin
        when (skin) {
            BallSkin.EMOJI -> {
                Text(
                    text = ballColor.emojiGlyph,
                    fontSize = (size.value * 0.48f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            BallSkin.SPORTS -> {
                Text(
                    text = ballColor.sportsGlyph,
                    fontSize = (size.value * 0.46f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            BallSkin.GEMS -> {
                Text(
                    text = ballColor.gemGlyph,
                    fontSize = (size.value * 0.46f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            BallSkin.NEON -> {
                // Futuristic central dot accent
                Canvas(modifier = Modifier.size(size * 0.25f)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = this.size.minDimension / 2f
                    )
                }
            }
        }
    }
}
