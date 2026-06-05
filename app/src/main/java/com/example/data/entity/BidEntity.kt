package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bids")
data class BidEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val username: String, // Store name directly for easy viewing
    val itemId: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
