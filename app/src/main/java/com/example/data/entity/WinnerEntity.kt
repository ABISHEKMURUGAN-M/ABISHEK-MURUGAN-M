package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "winners")
data class WinnerEntity(
    @PrimaryKey val itemId: String, // One winner per auction item
    val itemName: String,
    val winner: String, // Winner username
    val winningBid: Double
)
