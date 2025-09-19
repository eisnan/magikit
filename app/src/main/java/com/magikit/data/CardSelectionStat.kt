package com.magikit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_selection_stats")
data class CardSelectionStat(
    @PrimaryKey val cardCode: String,
    val count: Int
)

