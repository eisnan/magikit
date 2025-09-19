package com.magikit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CardSelectionStatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: CardSelectionStat)

    @Query("UPDATE card_selection_stats SET count = count + 1 WHERE cardCode = :cardCode")
    suspend fun incrementCount(cardCode: String)

    @Query("SELECT * FROM card_selection_stats ORDER BY count DESC")
    suspend fun getAllStatsDesc(): List<CardSelectionStat>

    @Query("SELECT * FROM card_selection_stats WHERE cardCode = :cardCode LIMIT 1")
    suspend fun getStat(cardCode: String): CardSelectionStat?
}

