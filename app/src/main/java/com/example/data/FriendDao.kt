package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friend_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<FriendProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FriendProfile)

    @Query("SELECT * FROM current_user WHERE id = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<CurrentUser?>

    @Query("SELECT * FROM current_user WHERE id = 1 LIMIT 1")
    suspend fun getCurrentUserDirect(): CurrentUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCurrentUser(user: CurrentUser)

    @Query("DELETE FROM current_user WHERE id = 1")
    suspend fun logoutUser()

    @Query("SELECT * FROM hometown_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<HometownMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: HometownMemory)

    @Query("DELETE FROM friend_profiles WHERE isUserCreated = 1")
    suspend fun clearUserCreatedProfiles()
}
