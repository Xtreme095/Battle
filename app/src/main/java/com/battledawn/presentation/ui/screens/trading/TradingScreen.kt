package com.battledawn.presentation.ui.screens.trading

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
import com.battledawn.domain.model.ResourceBundle
import com.battledawn.domain.model.TradeOffer
import com.battledawn.domain.model.TradeStatus
import com.battledawn.presentation.viewmodel.TradingViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Trading Screen
 * Create and manage resource trades with other players
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(
    onNavigateBack: () -> Unit,
    viewModel: TradingViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Incoming", "My Offers", "Create Trade")

    val incomingOffers by viewModel.incomingOffers.collectAsState()
    val activeOffers by viewModel.activeOffers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trading") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Trade")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(title)
                                if (index == 0 && incomingOffers.isNotEmpty()) {
                                    Badge { Text("${incomingOffers.size}") }
                                }
                            }
                        }
                    )
                }
            }

            // Content
            when (selectedTab) {
                0 -> IncomingOffersTab(
                    offers = incomingOffers,
                    onAccept = { viewModel.acceptTradeOffer(it) },
                    onReject = { viewModel.rejectTradeOffer(it) }
                )
                1 -> MyOffersTab(
                    offers = activeOffers,
                    onCancel = { viewModel.cancelTradeOffer(it) }
                )
                2 -> CreateTradeTab(
                    onCreateTrade = { receiver, offered, requested ->
                        viewModel.createTradeOffer(receiver, offered, requested)
                    },
                    viewModel = viewModel
                )
            }
        }
    }

    // Create Trade Dialog
    if (showCreateDialog) {
        CreateTradeDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { receiver, offered, requested ->
                viewModel.createTradeOffer(receiver, offered, requested)
                showCreateDialog = false
            }
        )
    }

    // Show messages
    LaunchedEffect(uiState) {
        // Handle UI state changes
    }
}

@Composable
private fun IncomingOffersTab(
    offers: List<TradeOffer>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (offers.isEmpty()) {
        EmptyState(
            icon = Icons.Default.SwapHoriz,
            title = "No Incoming Offers",
            subtitle = "You have no trade offers at the moment"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(offers) { offer ->
                TradeOfferCard(
                    offer = offer,
                    isIncoming = true,
                    onAccept = { onAccept(offer.id) },
                    onReject = { onReject(offer.id) }
                )
            }
        }
    }
}

@Composable
private fun MyOffersTab(
    offers: List<TradeOffer>,
    onCancel: (String) -> Unit
) {
    if (offers.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Send,
            title = "No Active Offers",
            subtitle = "Create a trade offer to get started"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(offers) { offer ->
                TradeOfferCard(
                    offer = offer,
                    isIncoming = false,
                    onCancel = { onCancel(offer.id) }
                )
            }
        }
    }
}

@Composable
private fun CreateTradeTab(
    onCreateTrade: (String, ResourceBundle, ResourceBundle) -> Unit,
    viewModel: TradingViewModel
) {
    var receiverUsername by remember { mutableStateOf("") }
    var offeredMetal by remember { mutableStateOf("") }
    var offeredOil by remember { mutableStateOf("") }
    var offeredEnergy by remember { mutableStateOf("") }
    var offeredFood by remember { mutableStateOf("") }
    var requestedMetal by remember { mutableStateOf("") }
    var requestedOil by remember { mutableStateOf("") }
    var requestedEnergy by remember { mutableStateOf("") }
    var requestedFood by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = receiverUsername,
            onValueChange = { receiverUsername = it },
            label = { Text("Receiver Username") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        Text(
            text = "You Offer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = offeredMetal,
                onValueChange = { offeredMetal = it.filter { c -> c.isDigit() } },
                label = { Text("Metal") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = offeredOil,
                onValueChange = { offeredOil = it.filter { c -> c.isDigit() } },
                label = { Text("Oil") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = offeredEnergy,
                onValueChange = { offeredEnergy = it.filter { c -> c.isDigit() } },
                label = { Text("Energy") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = offeredFood,
                onValueChange = { offeredFood = it.filter { c -> c.isDigit() } },
                label = { Text("Food") },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "You Request",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = requestedMetal,
                onValueChange = { requestedMetal = it.filter { c -> c.isDigit() } },
                label = { Text("Metal") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = requestedOil,
                onValueChange = { requestedOil = it.filter { c -> c.isDigit() } },
                label = { Text("Oil") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = requestedEnergy,
                onValueChange = { requestedEnergy = it.filter { c -> c.isDigit() } },
                label = { Text("Energy") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = requestedFood,
                onValueChange = { requestedFood = it.filter { c -> c.isDigit() } },
                label = { Text("Food") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val offered = ResourceBundle(
                    metal = offeredMetal.toLongOrNull() ?: 0,
                    oil = offeredOil.toLongOrNull() ?: 0,
                    energy = offeredEnergy.toLongOrNull() ?: 0,
                    food = offeredFood.toLongOrNull() ?: 0
                )
                val requested = ResourceBundle(
                    metal = requestedMetal.toLongOrNull() ?: 0,
                    oil = requestedOil.toLongOrNull() ?: 0,
                    energy = requestedEnergy.toLongOrNull() ?: 0,
                    food = requestedFood.toLongOrNull() ?: 0
                )
                onCreateTrade(receiverUsername, offered, requested)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = receiverUsername.isNotBlank()
        ) {
            Text("Create Trade Offer")
        }
    }
}

@Composable
private fun TradeOfferCard(
    offer: TradeOffer,
    isIncoming: Boolean,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isIncoming) "From: ${offer.senderUsername}" else "To: ${offer.receiverUsername}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimeRemaining(offer.expiresAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Trade details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isIncoming) "They Offer" else "You Offer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    ResourceDisplay(offer.offeredResources)
                }

                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Trade",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isIncoming) "They Request" else "You Request",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ResourceDisplay(offer.requestedResources)
                }
            }

            // Actions
            if (isIncoming && onAccept != null && onReject != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                }
            }

            if (!isIncoming && onCancel != null) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Offer")
                }
            }
        }
    }
}

@Composable
private fun ResourceDisplay(bundle: ResourceBundle) {
    Column {
        if (bundle.metal > 0) Text("Metal: ${bundle.metal}")
        if (bundle.oil > 0) Text("Oil: ${bundle.oil}")
        if (bundle.energy > 0) Text("Energy: ${bundle.energy}")
        if (bundle.food > 0) Text("Food: ${bundle.food}")
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreateTradeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ResourceBundle, ResourceBundle) -> Unit
) {
    // Simplified dialog - use CreateTradeTab instead
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Trade") },
        text = { Text("Use the 'Create Trade' tab to create a new trade offer") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun formatTimeRemaining(expiresAt: Instant): String {
    val now = Instant.now()
    val remaining = java.time.Duration.between(now, expiresAt)

    return when {
        remaining.isNegative -> "Expired"
        remaining.toHours() > 24 -> "${remaining.toDays()}d remaining"
        remaining.toHours() > 0 -> "${remaining.toHours()}h remaining"
        else -> "${remaining.toMinutes()}m remaining"
    }
}
