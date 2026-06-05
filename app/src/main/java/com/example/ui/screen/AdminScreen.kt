package com.example.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.UserEntity
import com.example.ui.AuctionViewModel
import com.example.ui.Screen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.SecondaryRed

@Composable
fun AdminScreen(
    viewModel: AuctionViewModel,
    items: List<AuctionItemEntity>,
    activeItem: AuctionItemEntity?,
    isBiddingActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Collect security context & database entities
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allBids by viewModel.allBids.collectAsStateWithLifecycle()
    val winnersList by viewModel.winnersList.collectAsStateWithLifecycle()

    // 🔒 Security access control gate
    if (currentUser == null || !currentUser!!.isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Access Denied",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Text(
                        text = "🔒 SECURITY ACCESS DENIED",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Access is restricted. Altering bid subjects, dropping player pool files, and hard overriding user bank boundaries requires cryptographically signed Administrator JWT authentication.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.navigateTo(Screen.ProfileSetup) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_go_to_auth_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authenticate via Access Portal", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
        return
    }

    // Custom Player Creation Form Fields
    var newPlayerName by remember { mutableStateOf("") }
    var newPlayerBasePrice by remember { mutableStateOf("") }
    var newPlayerCategory by remember { mutableStateOf("Batsman") }
    var newPlayerDescription by remember { mutableStateOf("") }
    var formStatusMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Batsman", "Bowler", "All-Rounder", "Wicket keeper")

    // Expansi State Holders for dashboards
    var showBidsLedger by remember { mutableStateOf(false) }
    var showUserManagement by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Upper back navigation bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Home) },
                    modifier = Modifier.testTag("admin_back_nav")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "COMMAND HUB CONTROL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- SECTION 1: ACTIVE HAMMER MASTER ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (isBiddingActive) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LIVE SESSION SUPERVISOR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isBiddingActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ROUND ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (activeItem != null) {
                        // Display Information about the currently active bidding subject
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = activeItem.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Base Category: ${activeItem.category} | ₹${activeItem.basePrice.toInt()} Cr",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Close Overrides Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.stopAuctionManually(asSold = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_declare_sold_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("DECLARE SOLD", fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            Button(
                                onClick = { viewModel.stopAuctionManually(asSold = false) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_declare_unsold_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("WIPE UNSOLD", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        // Empty/Idle session helper
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Bidding Session is currently idle.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Button(
                                onClick = { viewModel.startNextPlayerAuction() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_start_next_queue"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LAUNCH NEXT UPCOMING PLAYER", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // --- NEW! USER REGISTRY & BUDGET MANAGE SECTION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUserManagement = !showUserManagement },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Filled.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "MANAGE USERS & BUDGETS (${allUsers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = if (showUserManagement) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showUserManagement) {
                        Divider()
                        
                        if (allUsers.isEmpty()) {
                            Text("No users found in database.", fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                        } else {
                            allUsers.forEach { user ->
                                var editValue by remember { mutableStateOf(user.budget.toInt().toString()) }
                                val usersWonItems = winnersList.filter { it.winner == user.username }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(user.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            if (user.isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                            else if (user.isVirtual) Color.Gray.copy(alpha = 0.15f)
                                                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (user.isAdmin) "ADMIN" else if (user.isVirtual) "BOT" else "HUMAN",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (user.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                                    )
                                                }
                                            }
                                            Text(user.email, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                        }

                                        // Kick profile action (Only if it's not the active admin themselves)
                                        if (user.id != currentUser?.id) {
                                            IconButton(
                                                onClick = { viewModel.deleteUser(user.id) },
                                                modifier = Modifier.size(32.dp).testTag("delete_user_${user.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Drop user",
                                                    tint = SecondaryRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Display list of items won by this bidder
                                    if (usersWonItems.isNotEmpty()) {
                                        Text(
                                            text = "🏆 Won player(s): " + usersWonItems.joinToString(", ") { it.itemName },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ChampionshipGold,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }

                                    // Budget Modification Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Limit:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        OutlinedTextField(
                                            value = editValue,
                                            onValueChange = { editValue = it.filter { c -> c.isDigit() } },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .width(80.dp)
                                                .height(42.dp),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                            shape = RoundedCornerShape(6.dp),
                                            singleLine = true
                                        )
                                        Text("Cr", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                        Button(
                                            onClick = {
                                                val parsed = editValue.toDoubleOrNull() ?: 1000.0
                                                viewModel.updateUserBudget(user.id, parsed)
                                            },
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("save_user_budget_${user.id}"),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Save", modifier = Modifier.size(14.dp), tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("SET", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- NEW! CRITICAL BIDS LEDGER LOG SECTION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBidsLedger = !showBidsLedger },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Filled.FormatListNumbered, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "BIDS TRANSACTIONS LEDGER (${allBids.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = if (showBidsLedger) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (showBidsLedger) {
                        Divider()
                        
                        if (allBids.isEmpty()) {
                            Text(
                                text = "No bids have been recorded yet in this database instance.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allBids.forEach { bid ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(bid.username, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("on", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                                Text(bid.itemId, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(
                                                text = "Time token: ${bid.timestamp}", 
                                                fontSize = 9.sp, 
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                            )
                                        }

                                        Text(
                                            text = "₹${bid.amount.toInt()} Cr",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: ADD PLAYERS POOL FORM ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ADD CUSTOM PLAYERS TO SEED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Player Name
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = {
                            newPlayerName = it
                            formStatusMessage = null
                        },
                        label = { Text("Player Name") },
                        placeholder = { Text("e.g. Suryakumar Yadav") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_player_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Base Price (in Cr)
                    OutlinedTextField(
                        value = newPlayerBasePrice,
                        onValueChange = {
                            newPlayerBasePrice = it.filter { char -> char.isDigit() || char == '.' }
                            formStatusMessage = null
                        },
                        label = { Text("Base Price (in Cr)") },
                        placeholder = { Text("e.g., 50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_player_price_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Category Selector Combo-Box-like Tab Rows (for high Material density visual)
                    Text(
                        text = "Select Category Capsule",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = newPlayerCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { newPlayerCategory = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.substringBefore(" "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    // Description text input
                    OutlinedTextField(
                        value = newPlayerDescription,
                        onValueChange = {
                            newPlayerDescription = it
                            formStatusMessage = null
                        },
                        label = { Text("Player Profile Description") },
                        placeholder = { Text("Brief performance details...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_player_desc_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    // Validation status display
                    formStatusMessage?.let { status ->
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            color = if (status.startsWith("✅")) Color(0xFF4CAF50) else SecondaryRed,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }

                    // Form trigger button
                    Button(
                        onClick = {
                            if (newPlayerName.trim().isEmpty()) {
                                formStatusMessage = "❌ Name cannot be empty."
                            } else {
                                val baseVal = newPlayerBasePrice.toDoubleOrNull()
                                if (baseVal == null || baseVal <= 0.0) {
                                    formStatusMessage = "❌ Base Price must be a positive number."
                                } else {
                                    viewModel.addAuctionItem(
                                        name = newPlayerName.trim(),
                                        basePrice = baseVal,
                                        category = newPlayerCategory,
                                        description = newPlayerDescription.trim()
                                    )
                                    formStatusMessage = "✅ Player ${newPlayerName.trim()} added successfully to upcoming Queue list!"
                                    newPlayerName = ""
                                    newPlayerBasePrice = ""
                                    newPlayerDescription = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_player_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD TO AUCTION POOL", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // --- SECTION 3: SYSTEM OVERRIDES WIPE TOOLS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CRITICAL FACTORY RECOVERY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SecondaryRed
                    )
                    Text(
                        text = "Resets total local Room tables, terminates running coroutine flows, and seeds default players and virtual bidders for a fresh simulation start.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.resetAllData() },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryRed.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, SecondaryRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("factory_reset_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = SecondaryRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WIPE & RE-SEED DATABASE", fontWeight = FontWeight.Black, color = SecondaryRed)
                    }
                }
            }
        }

        // --- SECTION 4: QUEUE MONITORING LIST ---
        item {
            Text(
                text = "PERSISTENT DATABASE TABLE STATUS",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        items(items) { item ->
            AdminQueueRowItem(
                item = item, 
                onLaunch = { viewModel.startBiddingFor(item) },
                onDelete = { viewModel.removeAuctionItem(item.id) }
            )
        }
    }
}

@Composable
fun AdminQueueRowItem(
    item: AuctionItemEntity, 
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.category} | Base Price: ₹${item.basePrice.toInt()} Cr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Status capsule or launch action
            when (item.status) {
                "UPCOMING" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onLaunch,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("launch_item_${item.id}")
                        ) {
                            Text("LAUNCH", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }

                        // Remove button
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("delete_item_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete item",
                                tint = SecondaryRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                "ACTIVE" -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF9800))
                    }
                }
                "SOLD" -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "SOLD", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                    }
                }
                "UNSOLD" -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SecondaryRed.copy(alpha = 0.15f))
                            .border(1.dp, SecondaryRed, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "UNSOLD", fontSize = 10.sp, fontWeight = FontWeight.Black, color = SecondaryRed)
                    }
                }
            }
        }
    }
}
