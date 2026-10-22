package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.logic.BallSortLogic
import com.example.model.BallSkin
import com.example.model.TubeTheme
import kotlin.math.roundToInt

@Composable
fun TubeComposable(
    index: Int,
    balls: List<Int>,
    isSelected: Boolean,
    isHintSource: Boolean,
    isHintTarget: Boolean,
    isShaking: Boolean,
    ballSkin: BallSkin,
    tubeTheme: TubeTheme,
    ballSize: Dp = 26.dp,
    onTubeClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isComplete = BallSortLogic.isTubeComplete(balls)

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 240,
                    easing = LinearEasing
                )
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "tube_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_pulse"
    )

    // Reduced & optimized dimensions for 7 balls
    val tubeWidth = ballSize + 10.dp
    val tubeHeight = ballSize * BallSortLogic.TUBE_CAPACITY + 16.dp
    val headroom = ballSize + 16.dp

    val rimColor = when {
        isComplete -> Color(0xFF00E676)
        isSelected -> Color(0xFF00E5FF)
        isHintTarget -> Color(0xFFFFEA00).copy(alpha = pulseAlpha)
        isHintSource -> Color(0xFFFF2A55).copy(alpha = pulseAlpha)
        else -> Color(tubeTheme.rimColor).copy(alpha = 0.6f)
    }

    val tubeShape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 18.dp, bottomEnd = 18.dp)

    Column(
        modifier = modifier
            .widthIn(min = 48.dp)
            .offset { IntOffset(x = (if (isShaking) (Math.random() * 14 - 7).roundToInt() else 0), y = 0) }
            .testTag("tube_$index")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onTubeClicked(index) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Reserved headroom: Lifted ball animation and hint arrow are 100% visible
        Box(
            modifier = Modifier
                .size(width = tubeWidth, height = headroom),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (isHintTarget) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Hint target",
                    tint = Color(0xFFFFEA00),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(20.dp)
                )
            }

            if (isSelected && balls.isNotEmpty()) {
                val topColor = balls.last()
                BallComposable(
                    colorId = topColor,
                    skin = ballSkin,
                    size = ballSize,
                    isLifted = true,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Glass Tube Body
        Box(
            modifier = Modifier
                .width(tubeWidth)
                .height(tubeHeight)
                .shadow(
                    elevation = if (isSelected || isComplete) 8.dp else 3.dp,
                    shape = tubeShape,
                    ambientColor = rimColor,
                    spotColor = rimColor
                )
                .clip(tubeShape)
                .border(
                    width = if (isSelected || isComplete || isHintSource || isHintTarget) 3.dp else 1.8.dp,
                    color = rimColor,
                    shape = tubeShape
                )
        ) {
            // Glass Reflection & Interior Gradient
            Canvas(modifier = Modifier.fillMaxSize()) {
                val glassGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x18FFFFFF),
                        Color(0x08FFFFFF),
                        Color(0x2200E5FF).copy(alpha = if (isSelected) 0.25f else 0.08f)
                    )
                )
                drawRect(brush = glassGradient)

                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(4f, 8f),
                    end = Offset(4f, size.height - 20f),
                    strokeWidth = 2f
                )

                drawArc(
                    color = rimColor.copy(alpha = 0.4f),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(3f, size.height - 24f),
                    size = androidx.compose.ui.geometry.Size(size.width - 6f, 22f),
                    style = Stroke(width = 1.5f)
                )
            }

            // Balls column inside tube (rendered from bottom up)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom
            ) {
                val ballsToRender = if (isSelected && balls.isNotEmpty()) {
                    balls.dropLast(1)
                } else {
                    balls
                }

                val emptySlots = BallSortLogic.TUBE_CAPACITY - ballsToRender.size
                if (emptySlots > 0) {
                    Spacer(modifier = Modifier.weight(emptySlots.toFloat()))
                }

                ballsToRender.reversed().forEach { colorId ->
                    BallComposable(
                        colorId = colorId,
                        skin = ballSkin,
                        size = ballSize,
                        isLifted = false,
                        modifier = Modifier.padding(vertical = 0.8.dp)
                    )
                }
            }

            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Tube Completed",
                    tint = Color(0xFF00E676),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                        .shadow(6.dp, shape = RoundedCornerShape(14.dp), ambientColor = Color(0xFF00E676))
                )
            }
        }
    }
}
