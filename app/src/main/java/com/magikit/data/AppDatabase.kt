package com.magikit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CardSelectionStat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardSelectionStatDao(): CardSelectionStatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "card_stats_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

