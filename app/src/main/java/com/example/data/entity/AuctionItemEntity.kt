package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auction_items")
data class AuctionItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // e.g., "Batsman", "All-Rounder", "Bowler", "Rare Art"
    val basePrice: Double,
    val status: String // "ACTIVE", "SOLD", "UNSOLD", "UPCOMING"
)
