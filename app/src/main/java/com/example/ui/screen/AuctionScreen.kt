package com.example.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.UserEntity
import com.example.ui.AuctionViewModel
import com.example.ui.Screen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.SecondaryRed
import kotlinx.coroutines.launch

@Composable
fun AuctionScreen(
    viewModel: AuctionViewModel,
    currentUser: UserEntity?,
    activeItem: AuctionItemEntity?,
    currentBid: Double,
    highBidder: String?,
    timeLeft: Int,
    socketLogs: List<String>,
    isBiddingActive: Boolean,
    typingStatus: String?,
    bidError: String?,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val terminalListState = rememberLazyListState()

    // Auto-scroll the socket logs console to the bottom whenever a new log entry is registered
    LaunchedEffect(socketLogs.size) {
        if (socketLogs.isNotEmpty()) {
            terminalListState.animateScrollToItem(socketLogs.size - 1)
        }
    }

    // Interactive custom bid input state
    var customBidStr by remember { mutableStateOf("") }

    // Synchronize custom bid state with current live bid
    LaunchedEffect(currentBid) {
        customBidStr = (currentBid + 10.0).toInt().toString()
    }

    Scaffold(
        topBar = {
            // Live Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        modifier = Modifier.testTag("auction_back_nav")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIVE ARENA",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Glowing Socket.io Indicator
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isBiddingActive) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBiddingActive) "SOCKET.IO CONNECTED" else "AWAITING ENGINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Available budget hint pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (currentUser != null) "${currentUser.username}: ₹${currentUser.budget.toInt()} Cr" else "Spectating",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeItem == null) {
                // --- Empty State View when no auction represents active ---
                EmptyBiddingState(onNavigateToAdmin = { viewModel.navigateTo(Screen.AdminPanel) })
            } else {
                Spacer(modifier = Modifier.height(4.dp))

                // --- Live Board Frame ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category & Player Overview Group
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = activeItem.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = activeItem.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("active_player_name")
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = activeItem.description,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Base Price: ₹${activeItem.basePrice.toInt()} Cr",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Glowing Countdown Clock
                    CountdownCircle(timeLeft = timeLeft)
                }

                // --- Live Bidding Scoreboard ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CURRENT HIGHEST BID STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "₹${currentBid.toInt()} Cr",
                            fontWeight = FontWeight.Black,
                            fontSize = 44.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("current_bid_value")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // High Bidder username banner with crown or profile icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (highBidder == null) Icons.Filled.Cancel else Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = if (highBidder == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else ChampionshipGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (highBidder) {
                                    null -> "Awaiting Base Opener..."
                                    currentUser?.username -> "🏆 YOU ARE HOLDING THE BID!"
                                    else -> "Placed by $highBidder"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (highBidder == currentUser?.username) ChampionshipGold else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // --- Bidding Input Form & Adjustments ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Increments Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 25.0, 50.0, 100.0).forEach { inc ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        customBidStr = (currentBid + inc).toInt().toString()
                                        viewModel.clearBidError()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+₹${inc.toInt()} Cr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Bid Entry & Submission Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = customBidStr,
                            onValueChange = {
                                customBidStr = it.filter { c -> c.isDigit() }
                                viewModel.clearBidError()
                            },
                            placeholder = { Text("Amount in Cr") },
                            trailingIcon = { Text("Cr", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("bid_amount_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        )

                        Button(
                            onClick = {
                                val userPlacedAmount = customBidStr.toDoubleOrNull() ?: 0.0
                                viewModel.placeUserBid(userPlacedAmount)
                            },
                            enabled = isBiddingActive && timeLeft > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (highBidder == currentUser?.username) ChampionshipGold else MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .weight(1.8f)
                                .height(52.dp)
                                .testTag("place_bid_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Gavel,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (highBidder == currentUser?.username) "YOU ARE HIGH" else "PLACE LIVE BID",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.Black
                            )
                        }
                    }

                    // Real-Time Alert Messenger (Inside scope)
                    AnimatedVisibility(
                        visible = bidError != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        bidError?.let { err ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SecondaryRed.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Alert",
                                        tint = SecondaryRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = err,
                                        fontSize = 11.sp,
                                        color = SecondaryRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Typing / Thinking Indicator (Social aspect)
                    AnimatedVisibility(
                        visible = typingStatus != null,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        typingStatus?.let { status ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = status,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // --- Live Socket.io Events Ledger ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF07080C))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "SOCKET.IO BROADCAST FLOOD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "PORT: 443",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        state = terminalListState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (socketLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "Initializing channel socket buffer... Listening for events.",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            items(socketLogs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = when {
                                        log.contains("SOLD") -> ChampionshipGold
                                        log.contains("STARTED") -> MaterialTheme.colorScheme.primary
                                        log.contains("Warning") -> SecondaryRed
                                        log.contains("UNSOLD") -> SecondaryRed
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownCircle(timeLeft: Int) {
    val progress = (timeLeft.toFloat() / 15f).coerceIn(0f, 1f)
    val color = when {
        timeLeft <= 3 -> SecondaryRed
        timeLeft <= 7 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            strokeWidth = 6.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$timeLeft",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = "SEC",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EmptyBiddingState(onNavigateToAdmin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.HourglassEmpty,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ARENA CURRENTLY IDLE",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "No players are currently under the live hammer. Enter the Administrator Control Center to select a player and start their timer first!",
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToAdmin,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(48.dp)
                .testTag("launcher_to_admin_button")
        ) {
            Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "OPEN CONTROL CONSOLE",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Black
            )
        }
    }
}
