package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuctionRepository
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WinnerEntity
import com.example.simulation.AuctionSimulationEngine
import com.example.simulation.BidResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AuctionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuctionRepository
    val simulationEngine: AuctionSimulationEngine

    // --- Database Flows ---
    val allItems: StateFlow<List<AuctionItemEntity>>
    val allUsers: StateFlow<List<UserEntity>>
    val winnersList: StateFlow<List<WinnerEntity>>
    val allBids: StateFlow<List<com.example.data.entity.BidEntity>>

    // --- Active Client User ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // --- UI/UX Alerts ---
    private val _bidErrorMessage = MutableStateFlow<String?>(null)
    val bidErrorMessage: StateFlow<String?> = _bidErrorMessage.asStateFlow()

    // --- Navigation Screen Status ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AuctionRepository(database.auctionDao())
        simulationEngine = AuctionSimulationEngine(repository)

        // Read flows
        allItems = repository.allItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allUsers = repository.allUsers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        winnersList = repository.allWinners.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allBids = repository.allBids.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Run seeding and reload states
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            
            // Try auto-login last logged user if session token is valid, else set empty
            val users = repository.allUsers.firstOrNull() ?: emptyList()
            val savedHuman = users.firstOrNull { !it.isVirtual && it.authToken != null }
            if (savedHuman != null && com.example.auth.JwtUtils.parseAndValidateToken(savedHuman.authToken!!) != null) {
                _currentUser.value = savedHuman
            } else {
                _currentUser.value = null
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    /**
     * Set up human bidder session (Login / Signup Compatibility with default profile set).
     */
    fun loginOrRegister(username: String, email: String, budget: Double) {
        viewModelScope.launch {
            val formattedEmail = email.ifEmpty { "${username.lowercase()}@gmail.com" }
            val cleanBudget = if (budget <= 0) 1000.0 else budget

            val existing = repository.getUserByUsername(username)
            if (existing != null && !existing.isVirtual) {
                val token = com.example.auth.JwtUtils.generateToken(existing.username, existing.email, existing.isAdmin, existing.budget)
                val updated = existing.copy(authToken = token)
                repository.updateUser(updated)
                _currentUser.value = updated
            } else {
                val token = com.example.auth.JwtUtils.generateToken(username, formattedEmail, false, cleanBudget)
                val newUser = UserEntity(
                    id = "USER_${UUID.randomUUID().toString().take(6).uppercase()}",
                    username = username,
                    email = formattedEmail,
                    budget = cleanBudget,
                    isVirtual = false,
                    passwordHash = com.example.auth.JwtUtils.hashPassword("password123"),
                    isAdmin = false,
                    authToken = token
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
            }
            logSimulation("🧑‍💻 Bidder Profile Activated: $username | Budget: ₹${cleanBudget.toInt()} Cr")
        }
    }

    /**
     * Register a new user with fully secure password hash and dynamic JWT generation.
     */
    fun registerUser(
        username: String,
        email: String,
        password: String,
        budget: Double,
        isAdmin: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedName = username.trim()
            if (trimmedName.isEmpty()) {
                onError("Display Nickname cannot be empty.")
                return@launch
            }
            if (trimmedName.lowercase() in listOf("you", "abishek", "priya", "rahul", "vikram", "neha")) {
                onError("This username is reserved by bot simulation.")
                return@launch
            }
            val existing = repository.getUserByUsername(trimmedName)
            if (existing != null) {
                onError("Username already exists in the registry.")
                return@launch
            }

            val cleanEmail = email.trim().ifEmpty { "${trimmedName.lowercase()}@auctronix.live" }
            val cleanBudget = if (budget <= 0) 1000.0 else budget
            val hashedPass = com.example.auth.JwtUtils.hashPassword(password)
            val generatedToken = com.example.auth.JwtUtils.generateToken(trimmedName, cleanEmail, isAdmin, cleanBudget)

            val newUser = UserEntity(
                id = if (isAdmin) "ADMIN_${UUID.randomUUID().toString().take(6).uppercase()}" else "USER_${UUID.randomUUID().toString().take(6).uppercase()}",
                username = trimmedName,
                email = cleanEmail,
                budget = cleanBudget,
                isVirtual = false,
                passwordHash = hashedPass,
                isAdmin = isAdmin,
                authToken = generatedToken
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            logSimulation("🔐 User Registered Successfully [JWT Issued]: $trimmedName")
            onSuccess()
        }
    }

    /**
     * Authenticates an existing user and assigns an SHA-256 HMAC-signed JWT.
     */
    fun loginUser(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val trimmedName = username.trim()
            val existing = repository.getUserByUsername(trimmedName)
            if (existing == null || existing.isVirtual) {
                onError("User does not exist in registry.")
                return@launch
            }

            val hashedCheck = com.example.auth.JwtUtils.hashPassword(password)
            if (existing.passwordHash != hashedCheck) {
                onError("Incorrect password. Please try again.")
                return@launch
            }

            // Issue cryptographically signed token
            val newlySignedToken = com.example.auth.JwtUtils.generateToken(
                existing.username,
                existing.email,
                existing.isAdmin,
                existing.budget
            )
            val updatedUser = existing.copy(authToken = newlySignedToken)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            logSimulation("🔓 Authenticated: ${existing.username} [JWT Injected]")
            onSuccess()
        }
    }

    /**
     * Destroys active user JWT token and clears session state.
     */
    fun logoutUser() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                val updated = user.copy(authToken = null)
                repository.updateUser(updated)
            }
            _currentUser.value = null
            _bidErrorMessage.value = null
            navigateTo(Screen.Home)
        }
    }

    /**
     * Logs general messages to the Socket Logger
     */
    fun logSimulation(message: String) {
        viewModelScope.launch {
            // Log statement can optionally append to simulation logs
        }
    }

    /**
     * Let current human bidder place a bid.
     * Safely protected by cryptographical JWT signature verification checks!
     */
    fun placeUserBid(amount: Double) {
        _bidErrorMessage.value = null
        val user = _currentUser.value
        if (user == null || user.authToken == null) {
            _bidErrorMessage.value = "Unauthorized: Please log in or register to bid."
            return
        }

        // Crypto Signature parsing and validation check
        val claims = com.example.auth.JwtUtils.parseAndValidateToken(user.authToken)
        if (claims == null || claims.username != user.username) {
            _bidErrorMessage.value = "Unauthorized: Invalid or expired JWT token. Please re-authenticate."
            return
        }

        val active = simulationEngine.activeItem.value
        if (active == null) {
            _bidErrorMessage.value = "No active item represents an auction right now."
            return
        }

        // Check user budget
        if (user.budget < amount) {
            _bidErrorMessage.value = "Insufficient budget! You only have ₹${user.budget.toInt()} Cr left."
            return
        }

        val result = simulationEngine.placeBid(user.username, user.email, amount)
        if (result is BidResult.Error) {
            _bidErrorMessage.value = result.message
        }
    }

    /**
     * Clear bid error message after display.
     */
    fun clearBidError() {
        _bidErrorMessage.value = null
    }

    /**
     * Start auction for an item (Admin trigger).
     */
    fun startBiddingFor(item: AuctionItemEntity) {
        simulationEngine.startAuction(item)
    }

    /**
     * Stop auction manually or declare unsold (Admin control).
     */
    fun stopAuctionManually(asSold: Boolean) {
        simulationEngine.stopAuction(isSold = asSold)
    }

    /**
     * Delete an item from the auction pool (Admin control).
     */
    fun removeAuctionItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItemById(itemId)
            logSimulation("🗑️ Item Removed by Administrator: $itemId")
        }
    }

    /**
     * Delete a user profile from the database (Admin control).
     */
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUserById(userId)
            logSimulation("❌ User record deleted by Admin: $userId")
            if (_currentUser.value?.id == userId) {
                _currentUser.value = null
                _bidErrorMessage.value = null
                navigateTo(Screen.Home)
            }
        }
    }

    /**
     * Update user wallet budget dynamically (Admin control).
     */
    fun updateUserBudget(userId: String, newBudget: Double) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user != null) {
                val updated = user.copy(budget = newBudget)
                repository.updateUser(updated)
                logSimulation("💼 User budget updated: ${user.username} -> ₹${newBudget.toInt()} Cr")
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updated
                }
            }
        }
    }

    /**
     * Add players (Admin control).
     */
    fun addAuctionItem(name: String, basePrice: Double, category: String, description: String) {
        viewModelScope.launch {
            val newId = "ITEM_${UUID.randomUUID().toString().take(6).uppercase()}"
            val newItem = AuctionItemEntity(
                id = newId,
                name = name,
                basePrice = basePrice,
                category = category,
                description = description.ifEmpty { "A talented player up for grab in this auction round." },
                status = "UPCOMING"
            )
            repository.insertItem(newItem)
        }
    }

    /**
     * Selects and loads the next player in queue.
     */
    fun startNextPlayerAuction() {
        viewModelScope.launch {
            val all = repository.getAllItemsList()
            val upcoming = all.filter { it.status == "UPCOMING" }
            if (upcoming.isNotEmpty()) {
                val nextItem = upcoming.first()
                simulationEngine.startAuction(nextItem)
            } else {
                // If none upcoming, check unsold or just reload first for testing
                val unsolved = all.filter { it.status == "UNSOLD" }
                if (unsolved.isNotEmpty()) {
                    simulationEngine.startAuction(unsolved.first())
                }
            }
        }
    }

    /**
     * Reset database variables to clean seeded states.
     */
    fun resetAllData() {
        viewModelScope.launch {
            simulationEngine.stopAuction(isSold = false)
            repository.clearAllData()
            repository.seedDatabaseIfEmpty()

            // Reset current human state to match loaded human or clear
            _currentUser.value = null
            _bidErrorMessage.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationEngine.release()
    }
}

enum class Screen {
    Home,
    Auction,
    Results,
    AdminPanel,
    ProfileSetup
}
