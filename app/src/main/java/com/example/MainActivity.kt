package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuctionViewModel
import com.example.ui.Screen
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val mainViewModel: AuctionViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        MainAppContent(viewModel = mainViewModel)
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: AuctionViewModel) {
  // Collect all live states reactively from the ViewModel & Simulation Engine
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val bidError by viewModel.bidErrorMessage.collectAsStateWithLifecycle()

  val activeItem by viewModel.simulationEngine.activeItem.collectAsStateWithLifecycle()
  val currentBid by viewModel.simulationEngine.currentBid.collectAsStateWithLifecycle()
  val highBidder by viewModel.simulationEngine.currentHighBidder.collectAsStateWithLifecycle()
  val timeLeft by viewModel.simulationEngine.timeLeft.collectAsStateWithLifecycle()
  val socketLogs by viewModel.simulationEngine.socketLogs.collectAsStateWithLifecycle()
  val isBiddingActive by viewModel.simulationEngine.isBiddingActive.collectAsStateWithLifecycle()
  val typingStatus by viewModel.simulationEngine.chatTypingStatus.collectAsStateWithLifecycle()

  val allItems by viewModel.allItems.collectAsStateWithLifecycle()
  val winners by viewModel.winnersList.collectAsStateWithLifecycle()

  Scaffold(
    bottomBar = {
      // Bottom Navigation Bar mapping User Interface Modules
      NavigationBar(
        modifier = Modifier.navigationBarsPadding().testTag("bottom_nav_bar")
      ) {
        NavigationBarItem(
          selected = currentScreen == Screen.Home,
          onClick = { viewModel.navigateTo(Screen.Home) },
          icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
          label = { Text("Home") },
          modifier = Modifier.testTag("nav_home_tab")
        )

        NavigationBarItem(
          selected = currentScreen == Screen.Auction,
          onClick = { viewModel.navigateTo(Screen.Auction) },
          icon = { Icon(Icons.Filled.Gavel, contentDescription = "Auction Arena") },
          label = { Text("Bidding") },
          modifier = Modifier.testTag("nav_auction_tab")
        )

        NavigationBarItem(
          selected = currentScreen == Screen.Results,
          onClick = { viewModel.navigateTo(Screen.Results) },
          icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Winners Board") },
          label = { Text("Winners") },
          modifier = Modifier.testTag("nav_results_tab")
        )

        NavigationBarItem(
          selected = currentScreen == Screen.AdminPanel,
          onClick = { viewModel.navigateTo(Screen.AdminPanel) },
          icon = { Icon(Icons.Filled.Settings, contentDescription = "Command Console") },
          label = { Text("Admin") },
          modifier = Modifier.testTag("nav_admin_tab")
        )
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    val modifier = Modifier.padding(innerPadding)

    // Routing System displaying correct layouts
    when (currentScreen) {
      Screen.Home -> HomeScreen(
        viewModel = viewModel,
        currentUser = currentUser,
        items = allItems,
        winnersCount = winners.size,
        modifier = modifier
      )

      Screen.Auction -> AuctionScreen(
        viewModel = viewModel,
        currentUser = currentUser,
        activeItem = activeItem,
        currentBid = currentBid,
        highBidder = highBidder,
        timeLeft = timeLeft,
        socketLogs = socketLogs,
        isBiddingActive = isBiddingActive,
        typingStatus = typingStatus,
        bidError = bidError,
        modifier = modifier
      )

      Screen.Results -> WinnersScreen(
        viewModel = viewModel,
        winners = winners,
        modifier = modifier
      )

      Screen.AdminPanel -> AdminScreen(
        viewModel = viewModel,
        items = allItems,
        activeItem = activeItem,
        isBiddingActive = isBiddingActive,
        modifier = modifier
      )

      Screen.ProfileSetup -> ProfileSetupScreen(
        viewModel = viewModel,
        currentUser = currentUser,
        modifier = modifier
      )
    }
  }
}
