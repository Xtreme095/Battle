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
import com.battledawn.domain.model.*
import com.battledawn.presentation.ui.components.AllianceChatComponent
import com.battledawn.presentation.viewmodel.AllianceViewModel
import com.battledawn.presentation.viewmodel.DiplomacyViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val diplomacyViewModel: DiplomacyViewModel = hiltViewModel()
    val relationships by diplomacyViewModel.relationships.collectAsState()
    val incomingProposals by diplomacyViewModel.incomingProposals.collectAsState()
    val sentProposals by diplomacyViewModel.sentProposals.collectAsState()
    val reputation by diplomacyViewModel.allianceReputation.collectAsState()

    var showProposeDialog by remember { mutableStateOf(false) }
    var selectedProposalType by remember { mutableStateOf(ProposalType.NON_AGGRESSION_PACT) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Reputation Card
        item {
            reputation?.let { rep ->
                ReputationCard(rep)
            }
        }

        // Action Button
        item {
            Button(
                onClick = { showProposeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Handshake, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Diplomatic Proposal")
            }
        }

        // Incoming Proposals
        if (incomingProposals.isNotEmpty()) {
            item {
                Text(
                    text = "Incoming Proposals (${incomingProposals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(incomingProposals) { proposal ->
                ProposalCard(
                    proposal = proposal,
                    isIncoming = true,
                    onAccept = { diplomacyViewModel.acceptProposal(proposal.id) },
                    onReject = { diplomacyViewModel.rejectProposal(proposal.id) },
                    onCancel = { }
                )
            }
        }

        // Sent Proposals
        if (sentProposals.isNotEmpty()) {
            item {
                Text(
                    text = "Sent Proposals (${sentProposals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(sentProposals) { proposal ->
                ProposalCard(
                    proposal = proposal,
                    isIncoming = false,
                    onAccept = { },
                    onReject = { },
                    onCancel = { diplomacyViewModel.cancelProposal(proposal.id) }
                )
            }
        }

        // Current Relationships
        item {
            Text(
                text = "Diplomatic Relations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (relationships.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No diplomatic relationships",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(relationships) { relationship ->
                RelationshipCard(
                    relationship = relationship,
                    onBreakTreaty = { diplomacyViewModel.breakTreaty(relationship.id) }
                )
            }
        }
    }

    // Propose Dialog
    if (showProposeDialog) {
        ProposeDiplomacyDialog(
            onDismiss = { showProposeDialog = false },
            onConfirm = { targetName, proposalType, duration ->
                // TODO: In real implementation, get actual alliance ID from search
                val targetId = "target_alliance_id"
                when (proposalType) {
                    ProposalType.NON_AGGRESSION_PACT -> {
                        diplomacyViewModel.proposeNAP(targetId, targetName, duration)
                    }
                    ProposalType.PEACE_TREATY -> {
                        diplomacyViewModel.proposePeaceTreaty(targetId, targetName, duration)
                    }
                    ProposalType.ALLIANCE -> {
                        diplomacyViewModel.proposeAlliance(targetId, targetName, true, true)
                    }
                    ProposalType.WAR_DECLARATION -> {
                        diplomacyViewModel.declareWar(targetId, targetName)
                    }
                }
                showProposeDialog = false
            }
        )
    }
}

@Composable
fun ReputationCard(reputation: AllianceReputation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                        text = "Alliance Reputation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reputation.rank.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (reputation.rank) {
                            ReputationRank.HONORABLE -> MaterialTheme.colorScheme.primary
                            ReputationRank.TRUSTWORTHY -> MaterialTheme.colorScheme.primary
                            ReputationRank.NEUTRAL -> MaterialTheme.colorScheme.onSurface
                            ReputationRank.UNRELIABLE -> MaterialTheme.colorScheme.error
                            ReputationRank.DISHONORABLE -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                Text(
                    text = "${reputation.score}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = reputation.score / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn("Treaties Honored", "${reputation.treatiesHonored}")
                StatColumn("Treaties Broken", "${reputation.treatiesBroken}")
                StatColumn("Trustworthiness", "${(reputation.trustworthiness * 100).toInt()}%")
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProposalCard(
    proposal: DiplomaticProposal,
    isIncoming: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isIncoming) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (proposal.proposalType) {
                                ProposalType.NON_AGGRESSION_PACT -> Icons.Default.Shield
                                ProposalType.PEACE_TREATY -> Icons.Default.Handshake
                                ProposalType.ALLIANCE -> Icons.Default.Group
                                ProposalType.WAR_DECLARATION -> Icons.Default.WarningAmber
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = proposal.proposalType.name.replace("_", " "),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isIncoming) "From: ${proposal.proposerName}"
                               else "To: ${proposal.targetName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Duration: ${proposal.terms.duration.name.replace("_", " ")} (${proposal.terms.duration.days} days)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Expires: ${formatInstant(proposal.expiresAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (proposal.status) {
                        ProposalStatus.PENDING -> MaterialTheme.colorScheme.primaryContainer
                        ProposalStatus.ACCEPTED -> MaterialTheme.colorScheme.tertiaryContainer
                        ProposalStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                        ProposalStatus.EXPIRED -> MaterialTheme.colorScheme.surfaceVariant
                        ProposalStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = proposal.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (proposal.status == ProposalStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isIncoming) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RelationshipCard(
    relationship: DiplomaticRelationship,
    onBreakTreaty: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (relationship.status) {
                                DiplomaticStatus.NEUTRAL -> Icons.Default.Remove
                                DiplomaticStatus.NON_AGGRESSION -> Icons.Default.Shield
                                DiplomaticStatus.ALLIED -> Icons.Default.Favorite
                                DiplomaticStatus.AT_WAR -> Icons.Default.WarningAmber
                                DiplomaticStatus.PEACE_PENDING -> Icons.Default.Handshake
                                DiplomaticStatus.NAP_PENDING -> Icons.Default.Shield
                            },
                            contentDescription = null,
                            tint = when (relationship.status) {
                                DiplomaticStatus.ALLIED -> MaterialTheme.colorScheme.primary
                                DiplomaticStatus.AT_WAR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = relationship.alliance2Name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = relationship.status.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (relationship.status) {
                            DiplomaticStatus.ALLIED -> MaterialTheme.colorScheme.primary
                            DiplomaticStatus.AT_WAR -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    relationship.expiresAt?.let { expires ->
                        Text(
                            text = "Expires: ${formatInstant(expires)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (relationship.status != DiplomaticStatus.AT_WAR && relationship.status != DiplomaticStatus.NEUTRAL) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBreakTreaty,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Break Treaty")
                }
            }
        }
    }
}

@Composable
fun ProposeDiplomacyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ProposalType, DiplomaticDuration) -> Unit
) {
    var targetAllianceName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ProposalType.NON_AGGRESSION_PACT) }
    var selectedDuration by remember { mutableStateOf(DiplomaticDuration.MEDIUM_TERM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Diplomatic Proposal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetAllianceName,
                    onValueChange = { targetAllianceName = it },
                    label = { Text("Target Alliance Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Proposal Type",
                    style = MaterialTheme.typography.labelMedium
                )
                ProposalType.values().forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Text(
                            text = type.name.replace("_", " "),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                if (selectedType != ProposalType.WAR_DECLARATION) {
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelMedium
                    )
                    DiplomaticDuration.values().forEach { duration ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration }
                            )
                            Text(
                                text = "${duration.name.replace("_", " ")} (${duration.days} days)",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(targetAllianceName, selectedType, selectedDuration) },
                enabled = targetAllianceName.isNotBlank()
            ) {
                Text("Send Proposal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatInstant(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
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
