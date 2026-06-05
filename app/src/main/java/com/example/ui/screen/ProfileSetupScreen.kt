package com.example.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.ui.AuctionViewModel
import com.example.ui.Screen

@Composable
fun ProfileSetupScreen(
    viewModel: AuctionViewModel,
    currentUser: UserEntity?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register
    
    // Form States
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regBudgetStr by remember { mutableStateOf("1000") }
    var regIsAdmin by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(true) }
    
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Back Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.Home) },
                modifier = Modifier.testTag("profile_back_nav")
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "SECURITY ACCESS PORTAL",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (currentUser != null && currentUser.authToken != null) {
            // VIEW MODE: Logged In Session Info
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (currentUser.isAdmin) Icons.Filled.AdminPanelSettings else Icons.Filled.Face,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = currentUser.username,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dynamic System Role tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (currentUser.isAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (currentUser.isAdmin) "🛡️ ADMINISTRATOR ACCOUNT" else "🏏 ACTIVE CRICKET BIDDER",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (currentUser.isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User Profile Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Linked Email:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentUser.email, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Balance Wallet:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${currentUser.budget.toInt()} Cr", fontWeight = FontWeight.Black, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Registered ID:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentUser.id, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }

            // JWT TOKEN REVEAL DRAWER
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HMAC-SHA256 JWT AUTH TOKEN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Text(
                        text = currentUser.authToken,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentUser.authToken))
                            statusMessage = "📋 JWT Token copied to clipboard successfully!"
                            statusIsError = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy token", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Encrypted Session JWT", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (statusMessage != null) {
                Text(
                    text = statusMessage!!,
                    color = if (statusIsError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Revoke & Logout button
            Button(
                onClick = {
                    viewModel.logoutUser()
                    statusMessage = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISCONNECT & REVOKE TOKEN", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
            }

        } else {
            // EDIT MODE: Tab Switcher (LogIn vs Register)
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        statusMessage = null
                    },
                    text = { Text("Secure Sign-In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        statusMessage = null
                    },
                    text = { Text("Register Account", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == 0) {
                // TAB 0: LOGIN FORM
                Text(
                    text = "Auctronix Credentials Gate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = loginUsername,
                    onValueChange = { loginUsername = it; statusMessage = null },
                    label = { Text("Display Username") },
                    placeholder = { Text("e.g. admin or standard name") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("login_username_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it; statusMessage = null },
                    label = { Text("Security Password") },
                    placeholder = { Text("Enter account password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 13.sp,
                        color = if (statusIsError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        if (loginUsername.trim().isEmpty() || loginPassword.trim().isEmpty()) {
                            statusIsError = true
                            statusMessage = "❌ Inputs cannot be empty!"
                        } else {
                            viewModel.loginUser(
                                username = loginUsername,
                                password = loginPassword,
                                onSuccess = {
                                    statusIsError = false
                                    statusMessage = "✅ Logged in successfully!"
                                    viewModel.navigateTo(Screen.Home)
                                },
                                onError = { error ->
                                    statusIsError = true
                                    statusMessage = "❌ $error"
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Filled.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VERIFY & REQUEST JWT", fontWeight = FontWeight.Bold)
                }
                
                Text(
                    text = "💡 Quick Access Hint: Default administrator account can be accessed via username: 'admin' and password: 'admin123'",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )

            } else {
                // TAB 1: REGISTRATION FORM
                Text(
                    text = "Auctronix Registry Center",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = regUsername,
                    onValueChange = { regUsername = it; statusMessage = null },
                    label = { Text("Display Username") },
                    placeholder = { Text("Enter your bid name handle") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_username_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it; statusMessage = null },
                    label = { Text("Optional Email Address") },
                    placeholder = { Text("name@example.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it; statusMessage = null },
                    label = { Text("Account Security Password") },
                    placeholder = { Text("Choose a password") },
                    leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = regBudgetStr,
                    onValueChange = { regBudgetStr = it.filter { c -> c.isDigit() }; statusMessage = null },
                    label = { Text("Starting Bank Limit Budget (in Cr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth().testTag("reg_budget_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Admin Flag Switch row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Register as Administrator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Enables admin console and control widgets.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = regIsAdmin,
                        onCheckedChange = { regIsAdmin = it },
                        modifier = Modifier.testTag("admin_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 13.sp,
                        color = if (statusIsError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        val budget = regBudgetStr.toDoubleOrNull() ?: 1000.0
                        if (regUsername.trim().isEmpty() || regPassword.trim().isEmpty()) {
                            statusIsError = true
                            statusMessage = "❌ Username and Password are required!"
                        } else {
                            viewModel.registerUser(
                                username = regUsername,
                                email = regEmail,
                                password = regPassword,
                                budget = budget,
                                isAdmin = regIsAdmin,
                                onSuccess = {
                                    statusIsError = false
                                    statusMessage = "✅ Profile registered and certified successfully!"
                                    viewModel.navigateTo(Screen.Home)
                                },
                                onError = { error ->
                                    statusIsError = true
                                    statusMessage = "❌ $error"
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_submit_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Filled.AppRegistration, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REGISTER & RETRIEVE JWT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
