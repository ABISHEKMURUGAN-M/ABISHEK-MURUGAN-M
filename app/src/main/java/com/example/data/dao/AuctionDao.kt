package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.AuctionItemEntity
import com.example.data.entity.BidEntity
import com.example.data.entity.UserEntity
import com.example.data.entity.WinnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuctionDao {

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isVirtual = 1")
    suspend fun getVirtualBots(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Auction Items ---
    @Query("SELECT * FROM auction_items")
    fun getAllItemsFlow(): Flow<List<AuctionItemEntity>>

    @Query("SELECT * FROM auction_items")
    suspend fun getAllItems(): List<AuctionItemEntity>

    @Query("SELECT * FROM auction_items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): AuctionItemEntity?

    @Query("SELECT * FROM auction_items WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveItem(): AuctionItemEntity?

    @Query("SELECT * FROM auction_items WHERE status = :status")
    fun getItemsByStatus(status: String): Flow<List<AuctionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: AuctionItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<AuctionItemEntity>)

    @Update
    suspend fun updateItem(item: AuctionItemEntity)

    @Query("UPDATE auction_items SET status = :status WHERE id = :itemId")
    suspend fun updateItemStatus(itemId: String, status: String)

    // --- Bids ---
    @Query("SELECT * FROM bids WHERE itemId = :itemId ORDER BY amount DESC")
    fun getBidsForItemFlow(itemId: String): Flow<List<BidEntity>>

    @Query("SELECT * FROM bids WHERE itemId = :itemId ORDER BY amount DESC")
    suspend fun getBidsForItem(itemId: String): List<BidEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: BidEntity)

    @Query("DELETE FROM bids WHERE itemId = :itemId")
    suspend fun deleteBidsForNewAuction(itemId: String)

    // --- Winners ---
    @Query("SELECT * FROM winners")
    fun getAllWinnersFlow(): Flow<List<WinnerEntity>>

    @Query("SELECT * FROM winners")
    suspend fun getAllWinners(): List<WinnerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWinner(winner: WinnerEntity)

    // --- Complex Transactions ---
    @Transaction
    suspend fun finalizeAuctionWinner(itemId: String, winnerUsername: String, finalBidAmount: Double) {
        // Find user & deduct budget
        val user = getUserByUsername(winnerUsername)
        if (user != null) {
            val updatedUser = user.copy(budget = (user.budget - finalBidAmount).coerceAtLeast(0.0))
            updateUser(updatedUser)
        }

        // Update item status
        val item = getItemById(itemId)
        if (item != null) {
            val updatedItem = item.copy(status = "SOLD")
            updateItem(updatedItem)

            // Insert winner record
            val winnerRecord = WinnerEntity(
                itemId = itemId,
                itemName = item.name,
                winner = winnerUsername,
                winningBid = finalBidAmount
            )
            insertWinner(winnerRecord)
        }
    }

    @Transaction
    suspend fun declareUnsold(itemId: String) {
        val item = getItemById(itemId)
        if (item != null) {
            val updatedItem = item.copy(status = "UNSOLD")
            updateItem(updatedItem)
        }
    }

    @Query("DELETE FROM auction_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Query("SELECT * FROM bids ORDER BY amount DESC")
    fun getAllBidsFlow(): Flow<List<BidEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("DELETE FROM auction_items")
    suspend fun clearAllItems()

    @Query("DELETE FROM bids")
    suspend fun clearAllBids()

    @Query("DELETE FROM winners")
    suspend fun clearAllWinners()
}
