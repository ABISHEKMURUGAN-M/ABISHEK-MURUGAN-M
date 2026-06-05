package com.example.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.UserEntity
import com.example.ui.AuctionViewModel
import com.example.ui.Screen

@Composable
fun HomeScreen(
    viewModel: AuctionViewModel,
    currentUser: UserEntity?,
    items: List<AuctionItemEntity>,
    winnersCount: Int,
    modifier: Modifier = Modifier
) {
    val unsoldAmt = items.count { it.status == "UNSOLD" }
    val soldAmt = items.count { it.status == "SOLD" }
    val upcomingAmt = items.count { it.status == "UPCOMING" || it.status == "ACTIVE" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- Giant High-Energy Hero Branding ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = "Fast Action",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "AUCTRONIX LIVE",
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Bid Fast. Win Smart. Auction Live.",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A modern high-octane real-time sports & fantasy bidding simulator with live clock sync, competitive AI bots, and offline results logging.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }
        }

        // --- Active Profile Status Banner ---
        item {
            val isAuthenticated = currentUser != null && currentUser.authToken != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        if (isAuthenticated) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.navigateTo(Screen.ProfileSetup) }
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAuthenticated) {
                                    if (currentUser!!.isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                } else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAuthenticated) {
                                if (currentUser!!.isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Person
                            } else Icons.Filled.Lock,
                            contentDescription = "Avatar Icon",
                            tint = if (isAuthenticated) {
                                if (currentUser!!.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            } else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAuthenticated) {
                                if (currentUser!!.isAdmin) "🛡️ Admin: ${currentUser.username}" else "🏏 Bidder: ${currentUser.username}"
                            } else "🔒 Authentication Required",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAuthenticated) "Certified with JWT | Wallet Limit: ₹${currentUser.budget.toInt()} Cr" else "Tap here to log in & fetch secure JWT Token",
                            fontSize = 13.sp,
                            color = if (isAuthenticated) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f) else MaterialTheme.colorScheme.error
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Edit Profile",
                        tint = if (isAuthenticated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // --- Auction Dashboard Core Stats ---
        item {
            Column {
                Text(
                    text = "LIVE MARKET STATUS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats 1
                    StatItemCard(
                        title = "Sold Squad",
                        count = "$soldAmt",
                        icon = Icons.Filled.CheckCircle,
                        iconColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )

                    // Stats 2
                    StatItemCard(
                        title = "Unsold",
                        count = "$unsoldAmt",
                        icon = Icons.Filled.Cancel,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    // Stats 3
                    StatItemCard(
                        title = "Remaining",
                        count = "$upcomingAmt",
                        icon = Icons.Filled.FormatListBulleted,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Main Launcher Control Navigation List ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "INTERACTIVE ROOM CONSOLE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                // Navigation Pill 1: Live Bidding Screen
                ActionNavPill(
                    title = "Live Auction Arena",
                    subtitle = "Join the real-time bid battleground.",
                    icon = Icons.Filled.Gavel,
                    badgeText = if (viewModel.simulationEngine.isBiddingActive.value) "ACTIVE LIVE" else null,
                    badgeColor = MaterialTheme.colorScheme.secondary,
                    testTag = "join_auction_button",
                    onClick = { viewModel.navigateTo(Screen.Auction) }
                )

                // Navigation Pill 2: Leaderboard Results Screen
                ActionNavPill(
                    title = "Championship Board",
                    subtitle = "Verify winning results, item stats & histories.",
                    icon = Icons.Filled.EmojiEvents,
                    badgeText = if (winnersCount > 0) "$winnersCount WON" else null,
                    badgeColor = MaterialTheme.colorScheme.tertiary,
                    testTag = "results_board_button",
                    onClick = { viewModel.navigateTo(Screen.Results) }
                )

                // Navigation Pill 3: Admin Console
                ActionNavPill(
                    title = "Auction Command Center",
                    subtitle = "Start queue, add custom players, reset databases.",
                    icon = Icons.Filled.Settings,
                    testTag = "admin_panel_button",
                    onClick = { viewModel.navigateTo(Screen.AdminPanel) }
                )
            }
        }

        // --- Seed Setup Helper Quick Hint ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Pro Tip: Set up a Custom Profile with a specified budget in the Profile tab first to see your nickname in active bidding lists!",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatItemCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionNavPill(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    badgeColor: Color = Color.Red,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
