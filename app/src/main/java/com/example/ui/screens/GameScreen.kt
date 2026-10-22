package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.logic.BallSortLogic
import com.example.model.GameMode
import com.example.ui.components.ComboCallout
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.TubeComposable
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val completedTubes = state.tubes.count { BallSortLogic.isTubeComplete(it) }
    val totalPlayableTubes = state.colorsCount
    val progress = if (totalPlayableTubes > 0) {
        (completedTubes.toFloat() / totalPlayableTubes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1024),
                        Color(0xFF141738),
                        Color(0xFF090B18)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Game Mode & Level Tag
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1F234D),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Level",
                            tint = Color(0xFFFFEA00),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.gameMode == GameMode.RUSH) "RUSH BLITZ" else "LVL ${state.level}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                // Moves or Rush Timer
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1F234D),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (state.gameMode == GameMode.RUSH && state.rushTimeRemaining <= 20) Color(0xFFFF2A55) else Color(0xFF7B0091)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.gameMode == GameMode.RUSH) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (state.rushTimeRemaining <= 20) Color(0xFFFF2A55) else Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${state.rushTimeRemaining}s",
                                color = if (state.rushTimeRemaining <= 20) Color(0xFFFF2A55) else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        } else {
                            Text(
                                text = "MOVES: ${state.moves}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Coins & Shop Shortcut
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF2B2005),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.coins}",
                                color = Color(0xFFFFEA00),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Shop Button
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SHOP) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1F234D), CircleShape)
                            .testTag("shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Shop",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Crazy Soundboard Button
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.SOUNDBOARD) },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFF2A55), CircleShape)
                            .testTag("soundboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Crazy Soundboard",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Settings Button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1F234D), CircleShape)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Tube Completion Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF1F234D)
            )

            // Game Mode Switcher Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                GameMode.entries.forEach { mode ->
                    val isSelected = state.gameMode == mode
                    Surface(
                        onClick = { viewModel.setGameMode(mode) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF181B38),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = mode.title,
                            color = if (isSelected) Color(0xFF0D1024) else Color(0xFFB0B7DE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Main Tube Playing Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                // Compact tube & ball sizing for 7-ball capacity so ball animations are completely visible
                val ballSize = if (state.tubes.size <= 5) 28.dp else 25.dp

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (state.tubes.size <= 5) 36.dp else 32.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    maxItemsInEachRow = if (state.tubes.size <= 5) 3 else 4
                ) {
                    state.tubes.forEachIndexed { index, balls ->
                        val isSelected = state.selectedTube == index
                        val isHintSource = state.hint?.fromTubeIndex == index
                        val isHintTarget = state.hint?.toTubeIndex == index
                        val isShaking = state.shakingTube == index

                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                            TubeComposable(
                                index = index,
                                balls = balls,
                                isSelected = isSelected,
                                isHintSource = isHintSource,
                                isHintTarget = isHintTarget,
                                isShaking = isShaking,
                                ballSkin = state.selectedBallSkin,
                                tubeTheme = state.selectedTubeTheme,
                                ballSize = ballSize,
                                onTubeClicked = { viewModel.onTubeClicked(it) }
                            )
                        }
                    }
                }
            }

            // Bottom Actions Bar (Undo, Restart, Add Tube, Hint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo Button
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    enabled = state.canUndo,
                    onClick = { viewModel.undoMove() },
                    testTag = "undo_button"
                )

                // Restart Button
                ActionButton(
                    icon = Icons.Default.Refresh,
                    label = "Restart",
                    enabled = true,
                    onClick = { viewModel.restartLevel() },
                    testTag = "restart_button"
                )

                // Add Tube Button
                ActionButton(
                    icon = Icons.Default.Add,
                    label = if (state.canAddTube) "+Tube (${2 - state.extraTubesCount})" else "Max",
                    enabled = state.canAddTube,
                    onClick = { viewModel.addExtraTube() },
                    accentColor = Color(0xFF00E5FF),
                    testTag = "add_tube_button"
                )

                // Hint Button
                ActionButton(
                    icon = Icons.Default.Lightbulb,
                    label = "Hint",
                    enabled = true,
                    onClick = { viewModel.requestHint() },
                    accentColor = Color(0xFFFFEA00),
                    testTag = "hint_button"
                )
            }
        }

        // Floating Combo Callout Banner
        state.comboCalloutText?.let { callout ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ComboCallout(text = callout, visible = true)
            }
        }

        // Confetti Burst Overlay
        ConfettiOverlay(
            isActive = state.showConfetti,
            onFinished = { viewModel.clearConfetti() }
        )

        // Victory Dialog
        if (state.isWinModalVisible) {
            WinLevelModal(
                level = state.level,
                moves = state.moves,
                bestMoves = state.bestMoves,
                coinsEarned = 60 + state.comboStreak * 15,
                onNextLevel = { viewModel.nextLevel() },
                onReplay = { viewModel.restartLevel() }
            )
        }

        // Game Over Dialog (Rush Mode)
        if (state.isGameOver) {
            GameOverModal(
                score = state.rushScore,
                onRetry = { viewModel.setGameMode(GameMode.RUSH) },
                onBack = { viewModel.setGameMode(GameMode.CLASSIC) }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { showSettingsDialog = false }
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    accentColor: Color = Color.White,
    testTag: String = ""
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1F234D),
            disabledContainerColor = Color(0xFF141733).copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .border(
                1.2.dp,
                if (enabled) accentColor.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) accentColor else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun WinLevelModal(
    level: Int,
    moves: Int,
    bestMoves: Int,
    coinsEarned: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B38)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, Color(0xFF00E676), RoundedCornerShape(28.dp))
                .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF00E676))
                .testTag("win_modal")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LEVEL CLEARED! 🎉",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stars display
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color(0xFFFFEA00),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0D1024),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Moves used:", color = Color(0xFFB0B7DE))
                            Text("$moves", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (bestMoves > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Best moves:", color = Color(0xFFB0B7DE))
                                Text("$bestMoves", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reward:", color = Color(0xFFB0B7DE))
                            Text("+$coinsEarned 🪙", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextLevel,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("next_level_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NEXT LEVEL", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onReplay,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2F57)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("replay_button")
                ) {
                    Text("REPLAY", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameOverModal(
    score: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B38)),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, Color(0xFFFF2A55), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TIME'S UP! ⏰",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF2A55)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Final Rush Score: $score",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A55)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("TRY AGAIN ⚡", color = Color.White, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2F57)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MAIN GAME", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
