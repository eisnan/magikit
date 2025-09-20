package com.magikit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stack_positions")
data class StackPosition(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val position: Int,
    val cardCode: String?
)

