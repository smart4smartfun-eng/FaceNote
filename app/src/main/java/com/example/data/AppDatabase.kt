package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FriendProfile::class, HometownMemory::class, CurrentUser::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "facenote_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed data in background thread when DB is created
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.friendDao()
                        seedInitialData(dao)
                    }
                }
            }
        }

        suspend fun seedInitialData(dao: FriendDao) {
            // Seed 4 classic profiles from the FaceNote page:
            dao.insertProfile(
                FriendProfile(
                    id = 1,
                    name = "Sarah Jenkins",
                    hometown = "Class of 2012",
                    statusText = "Looking for old friends",
                    memoryDetail = "I am searching for old friends from high school! Does anyone remember the class field trip where we got lost in the museum or our senior year track meet relay team? Would love to catch up and see where everybody is now!",
                    avatarColorSeed = 0
                )
            )
            dao.insertProfile(
                FriendProfile(
                    id = 2,
                    name = "Michael Chang",
                    hometown = "Oakridge High",
                    statusText = "Added a hometown memory",
                    memoryDetail = "Does anyone remember Mr. Harrison's AP Chemistry classes? The time we accidentally scorched the lab ceiling was an absolute classic. Looking for members of the Oakridge High Class of 2010!",
                    avatarColorSeed = 1
                )
            )
            dao.insertProfile(
                FriendProfile(
                    id = 3,
                    name = "Jessica Taylor",
                    hometown = "Lincoln Neighborhood",
                    statusText = "Active 10m ago",
                    memoryDetail = "Grew up near 4th street from 1998 to 2008. Reconnecting with childhood neighbors who used to ride bicycles until the streetlights came on, or played tag by the old park. Let me know if you recall those times!",
                    avatarColorSeed = 2
                )
            )
            dao.insertProfile(
                FriendProfile(
                    id = 4,
                    name = "David Miller",
                    hometown = "Class of 2008",
                    statusText = "Looking for classmates",
                    memoryDetail = "I'm looking to reconnect with school mates from the 2008 graduating cohort! We are thinking about organizing a informal 18-year reunion next summer. Let me know if you were in the school band!",
                    avatarColorSeed = 3
                )
            )

            // Seed 2 initial memories on the board (everyone can read these!)
            dao.insertMemory(
                HometownMemory(
                    id = 1,
                    authorName = "Michael Chang",
                    hometownLocation = "Oakridge High",
                    memoryText = "The legendary Friday night football games under the massive stadium lights. Hot cocoa in paper cups and our school mascot running around."
                )
            )
            dao.insertMemory(
                HometownMemory(
                    id = 2,
                    authorName = "Jessica Taylor",
                    hometownLocation = "Lincoln Neighborhood",
                    memoryText = "Buying 5-cent candy from the small corner convenience store on 4th street during hot summer days. The store owner always knew all of our names!"
                )
            )
        }
    }
}
