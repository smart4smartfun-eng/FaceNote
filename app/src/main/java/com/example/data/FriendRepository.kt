package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FriendRepository(private val friendDao: FriendDao) {

    val allProfiles: Flow<List<FriendProfile>> = friendDao.getAllProfiles()
    val currentUser: Flow<CurrentUser?> = friendDao.getCurrentUserFlow()
    val allMemories: Flow<List<HometownMemory>> = friendDao.getAllMemories()

    suspend fun checkAndSeedIfEmpty() {
        // Double safety: if database is ever empty, seed it
        val profiles = friendDao.getAllProfiles().firstOrNull()
        if (profiles.isNullOrEmpty()) {
            AppDatabase.seedInitialData(friendDao)
        }
    }

    suspend fun insertProfile(profile: FriendProfile) {
        friendDao.insertProfile(profile)
    }

    suspend fun saveCurrentUser(user: CurrentUser) {
        friendDao.saveCurrentUser(user)
        // Also register them in the profiles list so others can find them!
        friendDao.insertProfile(
            FriendProfile(
                name = user.name,
                hometown = user.hometown,
                statusText = "Looking for friends",
                memoryDetail = user.searchIntent.ifBlank { "Looking to reconnect with old buddies from ${user.hometown}!" },
                avatarColorSeed = 4,
                isUserCreated = true
            )
        )
    }

    suspend fun logout() {
        friendDao.logoutUser()
        friendDao.clearUserCreatedProfiles()
    }

    suspend fun updateCurrentUserBalance(newBalance: Double) {
        val user = friendDao.getCurrentUserDirect()
        if (user != null) {
            friendDao.saveCurrentUser(user.copy(earningsBalance = newBalance))
        }
    }

    suspend fun insertMemory(memory: HometownMemory) {
        friendDao.insertMemory(memory)
    }
}
