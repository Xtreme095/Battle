package com.battledawn.presentation.ui.screens.research

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Research/Technology screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Military", "Economic", "Special")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Research Center") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Current Research
            CurrentResearchCard()

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Research list
            when (selectedTab) {
                0 -> MilitaryResearchList()
                1 -> EconomicResearchList()
                2 -> SpecialResearchList()
            }
        }
    }
}

@Composable
fun CurrentResearchCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Research",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advanced Armor",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = 0.65f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "65% Complete",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2h 15m remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MilitaryResearchList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(
            listOf(
                ResearchItem("Advanced Armor", "+15% Defense", true, false),
                ResearchItem("Advanced Weapons", "+15% Attack", false, true),
                ResearchItem("Tactical Training", "-20% Training Time", true, false),
                ResearchItem("Vehicle Engineering", "+10% Vehicle Stats", false, false),
                ResearchItem("Tank Warfare", "+20% Tank Stats", false, false)
            )
        ) { item ->
            ResearchCard(item)
        }
    }
}

@Composable
fun EconomicResearchList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(
            listOf(
                ResearchItem("Mining Efficiency", "+25% Metal Production", true, false),
                ResearchItem("Oil Extraction", "+25% Oil Production", false, true),
                ResearchItem("Energy Management", "+30% Energy Production", false, false),
                ResearchItem("Agricultural Science", "+30% Food Production", true, false)
            )
        ) { item ->
            ResearchCard(item)
        }
    }
}

@Composable
fun SpecialResearchList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(
            listOf(
                ResearchItem("Spy Training", "Unlocks Spy Units", false, true),
                ResearchItem("Nuclear Physics", "Unlocks Nuclear Missiles", false, false),
                ResearchItem("Ion Technology", "Unlocks Ion Cannon", false, false),
                ResearchItem("Scanner Tech", "Unlocks Scanner Arrays", true, false)
            )
        ) { item ->
            ResearchCard(item)
        }
    }
}

data class ResearchItem(
    val name: String,
    val bonus: String,
    val completed: Boolean,
    val inProgress: Boolean
)

@Composable
fun ResearchCard(item: ResearchItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.completed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.bonus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = "Cost: 5000 ⚙ 2000 🛢",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (item.inProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else if (!item.completed) {
                Button(onClick = { /* TODO: Start research */ }) {
                    Text("Research")
                }
            }
        }
    }
}
