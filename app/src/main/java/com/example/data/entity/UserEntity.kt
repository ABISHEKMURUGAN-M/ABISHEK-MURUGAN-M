package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val budget: Double,
    val isVirtual: Boolean = false, // Flag to distinguish virtual bots from real users
    val passwordHash: String = "",   // Store hashed password for security
    val isAdmin: Boolean = false,    // Admin role flag
    val authToken: String? = null    // Local copy of generated JWT token
)

