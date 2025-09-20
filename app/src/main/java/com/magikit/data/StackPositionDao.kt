package com.magikit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StackPositionDao {
    @Query("DELETE FROM stack_positions")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(positions: List<StackPosition>)

    @Query("SELECT * FROM stack_positions ORDER BY position ASC")
    suspend fun getAll(): List<StackPosition>
}

