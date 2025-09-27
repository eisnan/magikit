package com.magikit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magikit.data.AppDatabase
import com.magikit.data.CardSelectionStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val statDao = AppDatabase.getInstance(application).cardSelectionStatDao()
    private val _stats = MutableStateFlow<List<CardSelectionStat>>(emptyList())
    val stats: StateFlow<List<CardSelectionStat>> = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { statDao.getAllStatsDesc() }
            _stats.value = result
        }
    }
}

