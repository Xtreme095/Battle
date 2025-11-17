package com.battledawn.presentation.ui.screens.alliance

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.battledawn.presentation.ui.components.AllianceChatComponent
import com.battledawn.presentation.viewmodel.AllianceViewModel

/**
 * Alliance management screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllianceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AllianceViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Members", "Chat", "Diplomacy", "War")

    val chatMessages by viewModel.chatMessages.collectAsState()
    val currentAlliance by viewModel.currentAlliance.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alliance") },
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
            // Alliance info header
            AllianceInfoHeader()

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

            // Content
            when (selectedTab) {
                0 -> AllianceOverviewTab()
                1 -> AllianceMembersTab()
                2 -> {
                    // Chat Tab
                    AllianceChatComponent(
                        messages = chatMessages,
                        currentUsername = "Player", // TODO: Get from user state
                        onSendMessage = { message ->
                            viewModel.sendChatMessage(message)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                3 -> AllianceDiplomacyTab()
                4 -> AllianceWarTab()
            }
        }
    }
}

@Composable
fun AllianceInfoHeader() {
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
                Column {
                    Text(
                        text = "[WAR] Warriors of Armageddon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rank #5 | Members: 42/50",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AllianceOverviewTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            StatsCard()
            Spacer(modifier = Modifier.height(8.dp))
            RecentActivityCard()
        }
    }
}

@Composable
fun StatsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Alliance Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatRow("Total Power", "125,000")
            StatRow("Total Territories", "68")
            StatRow("Wins", "234")
            StatRow("Losses", "45")
            StatRow("Win Rate", "83.9%")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecentActivityCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActivityItem("Player123 conquered territory (45, 67)", "2h ago")
            ActivityItem("Alliance won battle vs [ENEMY]", "5h ago")
            ActivityItem("NewMember joined the alliance", "1d ago")
        }
    }
}

@Composable
fun ActivityItem(text: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AllianceMembersTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(
            listOf(
                Triple("Commander", 25, true),
                Triple("Player123", 22, false),
                Triple("WarLord99", 20, false),
                Triple("TankMaster", 18, false)
            )
        ) { (name, level, isLeader) ->
            MemberCard(name, level, isLeader)
        }
    }
}

@Composable
fun MemberCard(name: String, level: Int, isLeader: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isLeader) Icons.Default.Star else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isLeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Level $level ${if (isLeader) "• Leader" else ""}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (!isLeader) {
                IconButton(onClick = { /* TODO: Message */ }) {
                    Icon(Icons.Default.Email, contentDescription = "Message")
                }
            }
        }
    }
}

@Composable
fun AllianceDiplomacyTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Handshake,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Diplomacy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage treaties and alliances",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AllianceWarTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Wars",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WarItem("[ENEMY] Evil Empire", "3 days", "Winning")
                }
            }
        }
    }
}

@Composable
fun WarItem(enemy: String, duration: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "vs $enemy", fontWeight = FontWeight.Bold)
            Text(text = "Duration: $duration", style = MaterialTheme.typography.bodySmall)
        }
        Chip(text = status)
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
