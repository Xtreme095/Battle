package com.battledawn.presentation.ui.screens.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Military
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.battledawn.presentation.ui.theme.EnergyColor
import com.battledawn.presentation.ui.theme.FoodColor
import com.battledawn.presentation.ui.theme.MetalColor
import com.battledawn.presentation.ui.theme.OilColor

/**
 * Main game map screen showing the world
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToColony: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("World Map") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                // Resource display
                ResourceBar()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Build outpost */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Build Outpost")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0A0A0A))
        ) {
            // Game map grid
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cellX = ((offset.x - offsetX) / 40).toInt()
                            val cellY = ((offset.y - offsetY) / 40).toInt()
                            selectedCell = Pair(cellX, cellY)
                            // TODO: Show cell info or navigate to colony
                        }
                    }
            ) {
                val cellSize = 40f
                val gridSize = 100  // 100x100 grid

                // Draw grid
                for (x in 0 until gridSize) {
                    for (y in 0 until gridSize) {
                        val left = x * cellSize + offsetX
                        val top = y * cellSize + offsetY

                        // Only draw visible cells
                        if (left > -cellSize && left < size.width &&
                            top > -cellSize && top < size.height
                        ) {
                            // Grid cell color (vary terrain)
                            val terrainColor = when {
                                (x + y) % 5 == 0 -> Color(0xFF1A2A1A)  // Forest
                                (x + y) % 7 == 0 -> Color(0xFF2A2A1A)  // Desert
                                (x * y) % 11 == 0 -> Color(0xFF1A1A2A)  // Water
                                else -> Color(0xFF1A1A1A)  // Plains
                            }

                            drawRect(
                                color = terrainColor,
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(cellSize - 1f, cellSize - 1f)
                            )

                            // Draw selection
                            if (selectedCell?.first == x && selectedCell?.second == y) {
                                drawRect(
                                    color = Color.Yellow,
                                    topLeft = Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(cellSize - 1f, cellSize - 1f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            }

                            // Draw mock colonies (every 10th cell)
                            if (x % 10 == 5 && y % 10 == 5) {
                                drawCircle(
                                    color = Color.Red,
                                    radius = 8f,
                                    center = Offset(left + cellSize / 2, top + cellSize / 2)
                                )
                            }
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { onNavigateToColony("main_colony") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Home, contentDescription = "My Colony")
                }
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = { /* TODO: Units */ },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Military, contentDescription = "My Armies")
                }
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = { /* TODO: Alliance */ },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.People, contentDescription = "Alliance")
                }
            }
        }
    }
}

@Composable
fun ResourceBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ResourceDisplay(label = "Metal", value = "12.5K", color = MetalColor)
        Spacer(modifier = Modifier.width(8.dp))
        ResourceDisplay(label = "Oil", value = "8.2K", color = OilColor)
        Spacer(modifier = Modifier.width(8.dp))
        ResourceDisplay(label = "Energy", value = "5.1K", color = EnergyColor)
        Spacer(modifier = Modifier.width(8.dp))
        ResourceDisplay(label = "Food", value = "15.8K", color = FoodColor)
    }
}

@Composable
fun ResourceDisplay(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
