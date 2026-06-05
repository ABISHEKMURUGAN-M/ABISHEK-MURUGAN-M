package com.example.simulation

import android.util.Log
import com.example.data.AuctionRepository
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.BidEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class AuctionSimulationEngine(private val repository: AuctionRepository) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Live Socket States (Emulating Socket.io)
    private val _activeItem = MutableStateFlow<AuctionItemEntity?>(null)
    val activeItem: StateFlow<AuctionItemEntity?> = _activeItem.asStateFlow()

    private val _currentBid = MutableStateFlow<Double>(0.0)
    val currentBid: StateFlow<Double> = _currentBid.asStateFlow()

    private val _currentHighBidder = MutableStateFlow<String?>(null)
    val currentHighBidder: StateFlow<String?> = _currentHighBidder.asStateFlow()

    private val _timeLeft = MutableStateFlow<Int>(0)
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _socketLogs = MutableStateFlow<List<String>>(emptyList())
    val socketLogs: StateFlow<List<String>> = _socketLogs.asStateFlow()

    private val _isBiddingActive = MutableStateFlow<Boolean>(false)
    val isBiddingActive: StateFlow<Boolean> = _isBiddingActive.asStateFlow()

    // Active Bid list for the current item
    private val _activeBids = MutableStateFlow<List<BidEntity>>(emptyList())
    val activeBids: StateFlow<List<BidEntity>> = _activeBids.asStateFlow()

    private val _chatTypingStatus = MutableStateFlow<String?>(null)
    val chatTypingStatus: StateFlow<String?> = _chatTypingStatus.asStateFlow()

    // Jobs
    private var timerJob: Job? = null
    private var botBiddingJob: Job? = null

    init {
        logEvent("[System] Connected to Real-Time Auction Engine.")
    }

    private fun logEvent(message: String) {
        val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formattedLog = "[$timeStamp] $message"
        _socketLogs.value = (_socketLogs.value + formattedLog).takeLast(100)
    }

    /**
     * Start the auction for a specific item.
     */
    fun startAuction(item: AuctionItemEntity) {
        if (_isBiddingActive.value) {
            logEvent("⚠️ Cannot start new auction while another is in progress.")
            return
        }

        coroutineScope.launch {
            // Reset state
            _activeItem.value = item
            _currentBid.value = item.basePrice
            _currentHighBidder.value = null
            _timeLeft.value = 15 // Standard 15 second countdown
            _activeBids.value = emptyList()
            _isBiddingActive.value = true

            // Update item status in database to ACTIVE
            repository.updateItemStatus(item.id, "ACTIVE")
            repository.deleteBidsForNewAuction(item.id)

            logEvent("🔥 Live Auction STARTED for: ${item.name}!")
            logEvent("📈 Base Price: ₹${item.basePrice.toInt()} Cr | Category: ${item.category}")
            logEvent("⏳ 15 seconds remaining. Place your initial bids!")

            // Start timer countdown
            startTimer()

            // Start bot simulation
            startBotSimulation()
        }
    }

    /**
     * Stop the current auction manually (admin control).
     */
    fun stopAuction(isSold: Boolean = false) {
        timerJob?.cancel()
        botBiddingJob?.cancel()
        _chatTypingStatus.value = null

        val currItem = _activeItem.value
        val finalBid = _currentBid.value
        val winner = _currentHighBidder.value

        if (currItem != null && _isBiddingActive.value) {
            coroutineScope.launch {
                if (isSold && winner != null) {
                    repository.finalizeAuctionWinner(currItem.id, winner, finalBid)
                    logEvent("📢 SOLD! Winner: $winner | Winning Bid: ₹${finalBid.toInt()} Cr!")
                } else {
                    repository.declareUnsold(currItem.id)
                    logEvent("📢 UNSOLD! ${currItem.name} remains unsold.")
                }
                _isBiddingActive.value = false
                _timeLeft.value = 0
            }
        }
    }

    /**
     * Places a bid (comes from either current user or a bot).
     */
    fun placeBid(username: String, email: String, amount: Double): BidResult {
        val item = _activeItem.value ?: return BidResult.Error("No active item to bid on.")
        if (!_isBiddingActive.value || _timeLeft.value <= 0) {
            return BidResult.Error("Bidding is currently closed.")
        }

        // Validate bid increment (must be higher than current bid)
        val currentMax = _currentBid.value
        if (amount <= currentMax) {
            return BidResult.Error("Bid must be higher than current bid of ₹${currentMax.toInt()} Cr.")
        }

        // Validate bid increment steps (at least +5 or +10 for realism)
        val increment = amount - currentMax
        if (increment < 5.0 && currentMax > item.basePrice) {
            return BidResult.Error("Minimum increment is ₹5 Cr.")
        }

        // Synchronized check and placement
        synchronized(this) {
            if (amount <= _currentBid.value) {
                return BidResult.Error("Bid was outbid. Try placing a higher bid.")
            }

            _currentBid.value = amount
            _currentHighBidder.value = username

            // Reset timer to 15 seconds! (Countdown Based resets on bid)
            _timeLeft.value = 15
            logEvent("⚡ Bid Updated: $username places ₹${amount.toInt()} Cr! ⏳ Timer resets to 15s!")

            // Record in Database & local status
            coroutineScope.launch {
                val newBid = BidEntity(
                    userId = if (username == "You") "USER_ID" else "BOT_${username}",
                    username = username,
                    itemId = item.id,
                    amount = amount
                )
                repository.insertBid(newBid)

                // Update active bids local list
                val updatedBids = repository.getBidsForItem(item.id)
                _activeBids.value = updatedBids
            }
        }

        return BidResult.Success
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value = _timeLeft.value - 1

                val time = _timeLeft.value
                // Fire warnings at critical limits
                if (time == 10) {
                    logEvent("⏳ 10 seconds remaining...")
                } else if (time == 5) {
                    logEvent("⚠️ Warning: 5 seconds left! Going once...")
                } else if (time == 3) {
                    logEvent("🔊 Going twice... 3 seconds left!")
                } else if (time == 1) {
                    logEvent("🔊 Last call! 1 second remaining!")
                }
            }

            // Timer reached 0, end the auction
            val winner = _currentHighBidder.value
            val item = _activeItem.value
            if (item != null) {
                if (winner != null) {
                    stopAuction(isSold = true)
                } else {
                    stopAuction(isSold = false)
                }
            }
        }
    }

    private fun startBotSimulation() {
        botBiddingJob?.cancel()
        botBiddingJob = coroutineScope.launch {
            // Get virtual bot users from repository
            val bots = repository.getVirtualBots()
            if (bots.isEmpty()) return@launch

            while (_isBiddingActive.value && _timeLeft.value > 0) {
                // Bots sleep/think for a random interval (2 to 5 seconds)
                delay(Random.nextLong(2200, 4500))

                if (!_isBiddingActive.value || _timeLeft.value <= 0) break

                val activeItemVal = _activeItem.value ?: break
                val currentPrice = _currentBid.value
                val highBidder = _currentHighBidder.value

                // Choose a random bot who is NOT the current high bidder
                val eligibleBots = bots.filter { it.username != highBidder }
                if (eligibleBots.isEmpty()) continue

                val randomBot = eligibleBots.random()

                // Bot valuation logic: Bots don't bid if price exceeds their valuation threshold.
                // Valuation threshold is a random multiplier of the base price (between 2.5x and 4.0x)
                val valuationMultiplier = when (randomBot.username) {
                    "Vikram" -> 4.5
                    "Priya" -> 3.8
                    "Abishek" -> 3.5
                    "Neha" -> 3.2
                    else -> 2.8
                }
                val botMaxValuation = activeItemVal.basePrice * valuationMultiplier

                if (currentPrice >= botMaxValuation) {
                    // Bot exceeds its budget limit for this player - decline to bid
                    if (Random.nextFloat() < 0.25) {
                        _chatTypingStatus.value = "${randomBot.username} decided to back out."
                        delay(2000)
                        _chatTypingStatus.value = null
                    }
                    continue
                }

                // Check bot total budget constraints
                if (randomBot.budget < currentPrice + 10.0) {
                    continue
                }

                // Simulate Typing/Thinking status for extra socket visual flavor
                _chatTypingStatus.value = "${randomBot.username} is holding bidding paddle..."
                delay(Random.nextLong(800, 1500))
                _chatTypingStatus.value = null

                // Ensure condition didn't mutate during typing
                if (!_isBiddingActive.value || _timeLeft.value <= 0) break

                // Place a higher bid
                val increment = when {
                    currentPrice < 200.0 -> listOf(10.0, 15.0, 20.0, 25.0).random()
                    currentPrice < 500.0 -> listOf(20.0, 30.0, 50.0).random()
                    else -> listOf(50.0, 100.0).random()
                }

                val botBidAmount = currentPrice + increment

                // Last check: must not exceed bot budget
                if (botBidAmount <= randomBot.budget) {
                    placeBid(randomBot.username, randomBot.email, botBidAmount)
                }
            }
        }
    }

    fun release() {
        timerJob?.cancel()
        botBiddingJob?.cancel()
        _chatTypingStatus.value = null
    }
}

sealed class BidResult {
    object Success : BidResult()
    data class Error(val message: String) : BidResult()
}
