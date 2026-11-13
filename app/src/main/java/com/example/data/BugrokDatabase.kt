package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ServerProfileEntity::class, DownloadedPackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BugrokDatabase : RoomDatabase() {
    abstract fun serverProfileDao(): ServerProfileDao
    abstract fun downloadedPackDao(): DownloadedPackDao

    companion object {
        @Volatile
        private var INSTANCE: BugrokDatabase? = null

        fun getInstance(context: Context): BugrokDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BugrokDatabase::class.java,
                    "bugrok_tool_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
