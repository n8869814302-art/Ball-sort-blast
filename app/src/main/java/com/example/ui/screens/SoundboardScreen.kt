package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

data class SoundPad(
    val title: String,
    val icon: String,
    val description: String,
    val color: Color,
    val onPlay: () -> Unit
)

@Composable
fun SoundboardScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.GAME)
    }

    val soundPads = listOf(
        SoundPad(
            title = "DJ AIR HORN",
            icon = "📣",
            description = "LOUD & CRAZY stadium horn blast!",
            color = Color(0xFFFF2A55),
            onPlay = { viewModel.soundEngine.playAirhorn() }
        ),
        SoundPad(
            title = "VICTORY FANFARE",
            icon = "🏆",
            description = "Grand ascending heroic melody",
            color = Color(0xFFFFEA00),
            onPlay = { viewModel.soundEngine.playLevelWin() }
        ),
        SoundPad(
            title = "HYPER LASER",
            icon = "⚡",
            description = "High-voltage arcade streak beam",
            color = Color(0xFF00E5FF),
            onPlay = { viewModel.soundEngine.playComboStreak(4) }
        ),
        SoundPad(
            title = "FIREWORKS BOOM",
            icon = "🎆",
            description = "Sub-bass explosion & crackle",
            color = Color(0xFFFF8C00),
            onPlay = { viewModel.soundEngine.playFireworks() }
        ),
        SoundPad(
            title = "TUBE CHIME",
            icon = "✨",
            description = "Major arpeggio tube solve chime",
            color = Color(0xFF00E676),
            onPlay = { viewModel.soundEngine.playTubeComplete() }
        ),
        SoundPad(
            title = "POP CASCADE",
            icon = "🫧",
            description = "Rapid machine-gun bubble pops",
            color = Color(0xFF2979FF),
            onPlay = {
                viewModel.soundEngine.playCascade(0)
                viewModel.soundEngine.playCascade(1)
                viewModel.soundEngine.playCascade(2)
            }
        ),
        SoundPad(
            title = "CARTOON BOING",
            icon = "🤪",
            description = "Wobbly comical spring error",
            color = Color(0xFFD500F9),
            onPlay = { viewModel.soundEngine.playError() }
        ),
        SoundPad(
            title = "MAGIC PORTAL",
            icon = "🔮",
            description = "Ascending celestial shimmer",
            color = Color(0xFFFF1784),
            onPlay = { viewModel.soundEngine.playAddTube() }
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1024), Color(0xFF15183A), Color(0xFF090B18))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.GAME) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF1F234D), CircleShape)
                        .testTag("soundboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFFFF2A55),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CRAZY SOUNDBOARD",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.size(42.dp))
            }

            // Loudness & Crazy Mode Controls Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF181B38)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF2A55).copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFFFF2A55))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SFX LOUDNESS BOOST",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "${(state.volumeScale * 100).toInt()}% ${if (state.volumeScale > 1.0f) "🔥 CRAZY" else ""}",
                            fontWeight = FontWeight.Black,
                            color = if (state.volumeScale > 1.0f) Color(0xFFFF2A55) else Color(0xFF00E5FF),
                            fontSize = 14.sp
                        )
                    }

                    Slider(
                        value = state.volumeScale,
                        onValueChange = { viewModel.setVolumeScale(it) },
                        valueRange = 0.2f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF2A55),
                            activeTrackColor = Color(0xFFFF2A55),
                            inactiveTrackColor = Color(0xFF2B2F57)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "CRAZY MODE SFX 🔥",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Airhorn combos, fireworks solves, wild synth warbles",
                                color = Color(0xFF8D93B8),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = state.isCrazyMode,
                            onCheckedChange = { viewModel.toggleCrazyMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFFF2A55)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TAP PADS TO BLAST SFX 🔊",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00E5FF),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Sound Pads Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(soundPads) { pad ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B38)),
                        border = androidx.compose.foundation.BorderStroke(2.dp, pad.color.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = pad.color, spotColor = pad.color)
                            .clickable { pad.onPlay() }
                            .testTag("soundpad_${pad.title.lowercase().replace(' ', '_')}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = pad.icon, fontSize = 28.sp)
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(pad.color, CircleShape)
                                )
                            }

                            Column {
                                Text(
                                    text = pad.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = pad.description,
                                    fontSize = 10.sp,
                                    color = Color(0xFFB0B7DE),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
