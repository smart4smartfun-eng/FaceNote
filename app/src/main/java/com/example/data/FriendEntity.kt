package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend_profiles")
data class FriendProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val hometown: String,
    val statusText: String,
    val memoryDetail: String,
    val avatarColorSeed: Int, // 0 to 5 for different nice colors
    val isUserCreated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hometown_memories")
data class HometownMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorName: String,
    val hometownLocation: String,
    val memoryText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "current_user")
data class CurrentUser(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val hometown: String,
    val searchIntent: String,
    val earningsBalance: Double = 3.50
)
