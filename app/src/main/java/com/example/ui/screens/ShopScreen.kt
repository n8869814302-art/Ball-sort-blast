package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BallSkin
import com.example.model.TubeTheme
import com.example.ui.components.BallComposable
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.Screen

@Composable
fun ShopScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.GAME)
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1024), Color(0xFF131733), Color(0xFF090B18))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
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
                        .testTag("shop_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "ARCADE VAULT 🛒",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                // Coins
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF2B2005),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.coins}",
                            color = Color(0xFFFFEA00),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Tabs: Ball Skins vs Tube Themes
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF181B38),
                contentColor = Color(0xFF00E5FF),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .border(1.dp, Color(0xFF282D5C), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("BALL SKINS", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("TUBE STYLES", fontWeight = FontWeight.Bold) }
                )
            }

            // Content
            if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(BallSkin.entries) { skin ->
                        val isUnlocked = viewModel.isSkinUnlocked(skin)
                        val isEquipped = state.selectedBallSkin == skin

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B38)),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isEquipped) 2.dp else 1.dp,
                                if (isEquipped) Color(0xFF00E676) else Color(0xFF282D5C)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp))
                                .testTag("skin_item_${skin.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Ball Preview
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BallComposable(colorId = 0, skin = skin, size = 36.dp)
                                    BallComposable(colorId = 2, skin = skin, size = 36.dp)
                                    BallComposable(colorId = 4, skin = skin, size = 36.dp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = skin.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = skin.description,
                                        fontSize = 11.sp,
                                        color = Color(0xFF8D93B8),
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = { viewModel.selectBallSkin(skin) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            isEquipped -> Color(0xFF00E676)
                                            isUnlocked -> Color(0xFF00E5FF)
                                            state.coins >= skin.price -> Color(0xFFFFD700)
                                            else -> Color(0xFF282D5C)
                                        }
                                    )
                                ) {
                                    when {
                                        isEquipped -> {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                        }
                                        isUnlocked -> {
                                            Text("EQUIP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        else -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${skin.price}🪙", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(TubeTheme.entries) { theme ->
                        val isUnlocked = viewModel.isTubeUnlocked(theme)
                        val isEquipped = state.selectedTubeTheme == theme

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF181B38)),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isEquipped) 2.dp else 1.dp,
                                if (isEquipped) Color(0xFF00E676) else Color(0xFF282D5C)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 56.dp)
                                        .border(2.5.dp, Color(theme.rimColor), RoundedCornerShape(8.dp))
                                        .background(Color(theme.rimColor).copy(alpha = 0.15f))
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Custom glass glow & rim highlight",
                                        fontSize = 11.sp,
                                        color = Color(0xFF8D93B8)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.selectTubeTheme(theme) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            isEquipped -> Color(0xFF00E676)
                                            isUnlocked -> Color(0xFF00E5FF)
                                            state.coins >= theme.price -> Color(0xFFFFD700)
                                            else -> Color(0xFF282D5C)
                                        }
                                    )
                                ) {
                                    when {
                                        isEquipped -> {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                        }
                                        isUnlocked -> {
                                            Text("EQUIP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        else -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${theme.price}🪙", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
