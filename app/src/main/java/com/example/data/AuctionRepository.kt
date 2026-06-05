package com.example.data

import com.example.data.dao.AuctionDao
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.BidEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WinnerEntity
import kotlinx.coroutines.flow.Flow

class AuctionRepository(private val auctionDao: AuctionDao) {

    // --- Users ---
    val allUsers: Flow<List<UserEntity>> = auctionDao.getAllUsers()

    suspend fun getVirtualBots(): List<UserEntity> = auctionDao.getVirtualBots()

    suspend fun getUserById(userId: String): UserEntity? = auctionDao.getUserById(userId)
    suspend fun getUserByUsername(username: String): UserEntity? = auctionDao.getUserByUsername(username)
    suspend fun insertUser(user: UserEntity) = auctionDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = auctionDao.updateUser(user)
    suspend fun deleteUserById(userId: String) = auctionDao.deleteUserById(userId)

    // --- Items ---
    val allItems: Flow<List<AuctionItemEntity>> = auctionDao.getAllItemsFlow()

    suspend fun getAllItemsList(): List<AuctionItemEntity> = auctionDao.getAllItems()
    suspend fun getActiveItem(): AuctionItemEntity? = auctionDao.getActiveItem()
    fun getItemsByStatus(status: String): Flow<List<AuctionItemEntity>> = auctionDao.getItemsByStatus(status)
    suspend fun insertItem(item: AuctionItemEntity) = auctionDao.insertItem(item)
    suspend fun updateItem(item: AuctionItemEntity) = auctionDao.updateItem(item)
    suspend fun updateItemStatus(itemId: String, status: String) = auctionDao.updateItemStatus(itemId, status)
    suspend fun deleteItemById(itemId: String) = auctionDao.deleteItemById(itemId)

    // --- Bids ---
    val allBids: Flow<List<BidEntity>> = auctionDao.getAllBidsFlow()
    fun getBidsForItemFlow(itemId: String): Flow<List<BidEntity>> = auctionDao.getBidsForItemFlow(itemId)
    suspend fun getBidsForItem(itemId: String): List<BidEntity> = auctionDao.getBidsForItem(itemId)
    suspend fun insertBid(bid: BidEntity) = auctionDao.insertBid(bid)
    suspend fun deleteBidsForNewAuction(itemId: String) = auctionDao.deleteBidsForNewAuction(itemId)

    // --- Winners ---
    val allWinners: Flow<List<WinnerEntity>> = auctionDao.getAllWinnersFlow()
    suspend fun getAllWinnersList(): List<WinnerEntity> = auctionDao.getAllWinners()
    suspend fun insertWinner(winner: WinnerEntity) = auctionDao.insertWinner(winner)

    // --- Complex ---
    suspend fun finalizeAuctionWinner(itemId: String, winnerUsername: String, finalBidAmount: Double) =
        auctionDao.finalizeAuctionWinner(itemId, winnerUsername, finalBidAmount)

    suspend fun declareUnsold(itemId: String) = auctionDao.declareUnsold(itemId)

    suspend fun clearAllData() {
        auctionDao.clearAllBids()
        auctionDao.clearAllWinners()
        auctionDao.clearAllItems()
        auctionDao.clearAllUsers()
    }

    suspend fun seedDatabaseIfEmpty() {
        val items = auctionDao.getAllItems()
        if (items.isEmpty()) {
            val sampleItems = listOf(
                AuctionItemEntity("ITEM001", "Virat Kohli", "Elegant right-hand batsman, chase master, and modern-day cricket legend.", "Batsman", 100.0, "UPCOMING"),
                AuctionItemEntity("ITEM002", "M S Dhoni", "Thala, veteran wicketkeeper, ice-cool captain, and finisher par excellence.", "Wicket keeper", 150.0, "UPCOMING"),
                AuctionItemEntity("ITEM003", "Jasprit Bumrah", "Exceptional fast bowler with unique release point and lethal yorkers.", "Bowler", 120.0, "UPCOMING"),
                AuctionItemEntity("ITEM004", "Rashid Khan", "Afghan spin sensation, quick leg-breaks, dynamic lower-order hitting.", "Bowler", 100.0, "UPCOMING"),
                AuctionItemEntity("ITEM005", "Hardik Pandya", "Dynamic seam-bowling all-rounder with explosive power hitting capabilities.", "All-Rounder", 110.0, "UPCOMING"),
                AuctionItemEntity("ITEM006", "Glenn Maxwell", "The 'Big Show', eccentric 360-degree hitter, and handy off-spinner.", "All-Rounder", 90.0, "UPCOMING"),
                AuctionItemEntity("ITEM007", "Rohit Sharma", "The 'Hitman', effortless six-hitting ability, multiple IPL winning captain.", "Batsman", 130.0, "UPCOMING"),
                AuctionItemEntity("ITEM008", "A B de Villiers", "Mr. 360, legendary versatility, mind-boggling innovation with the bat.", "Batsman", 140.0, "UPCOMING"),
                AuctionItemEntity("ITEM009", "Pat Cummins", "World class pace bowler, outstanding leader, competitive lower-order bat.", "Bowler", 120.0, "UPCOMING"),
                AuctionItemEntity("ITEM010", "Gautam Gambhir", "Fiercely competitive batsman, major ICC finals match-winner.", "Batsman", 90.0, "UPCOMING")
            )
            auctionDao.insertItems(sampleItems)
        }

        // Check and seed default virtual users for bidding simulation
        val virtualUsers = listOf(
            UserEntity("BOT001", "Abishek", "abi@gmail.com", 1000.0, isVirtual = true),
            UserEntity("BOT002", "Priya", "priya@gmail.com", 1200.0, isVirtual = true),
            UserEntity("BOT003", "Rahul", "rahul@gmail.com", 900.0, isVirtual = true),
            UserEntity("BOT004", "Vikram", "vikram@gmail.com", 1500.0, isVirtual = true),
            UserEntity("BOT005", "Neha", "neha@gmail.com", 1100.0, isVirtual = true)
        )
        for (user in virtualUsers) {
            if (auctionDao.getUserById(user.id) == null) {
                auctionDao.insertUser(user)
            }
        }

        // Check and seed default Administrator
        val adminUser = UserEntity(
            id = "ADMIN001",
            username = "admin",
            email = "admin@auctronix.com",
            budget = 2000.0,
            isVirtual = false,
            passwordHash = com.example.auth.JwtUtils.hashPassword("admin123"),
            isAdmin = true,
            authToken = null
        )
        if (auctionDao.getUserById(adminUser.id) == null) {
            auctionDao.insertUser(adminUser)
        }
    }
}
