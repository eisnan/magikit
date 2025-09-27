package com.magikit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magikit.data.AppDatabase
import com.magikit.data.StackPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DesignStackViewModel(application: Application) : AndroidViewModel(application) {
    private val spotCount = 52
    private val db = AppDatabase.getInstance(application)
    private val stackDao = db.stackPositionDao()

    private val _assignedCards = MutableStateFlow(List<Card?>(spotCount) { null })
    val assignedCards: StateFlow<List<Card?>> = _assignedCards.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _selectedSpot = MutableStateFlow<Int?>(null)
    val selectedSpot: StateFlow<Int?> = _selectedSpot.asStateFlow()

    init {
        loadStack()
    }

    fun loadStack() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedStack = stackDao.getAll()
            if (savedStack.isNotEmpty()) {
                val loaded = List(spotCount) { idx ->
                    val pos = savedStack.find { it.position == idx }
                    pos?.cardCode?.let { code -> Card.entries.find { it.code == code } }
                }
                _assignedCards.value = loaded
            }
        }
    }

    fun onBack() { /* Navigation handled by composable */ }

    fun onFeelingLucky() {
        val shuffled = Card.entries.shuffled()
        _assignedCards.value = shuffled.take(spotCount)
    }

    fun onSaveStack() {
        viewModelScope.launch(Dispatchers.IO) {
            stackDao.clearAll()
            val stack = _assignedCards.value.mapIndexed { idx, card ->
                StackPosition(position = idx, cardCode = card?.code)
            }
            stackDao.insertAll(stack)
        }
    }

    fun onCellClick(index: Int) {
        if (availableCards().isNotEmpty()) {
            _selectedSpot.value = index
            _showDialog.value = true
        }
    }

    fun onCellLongClick(index: Int) {
        val updated = _assignedCards.value.toMutableList().also { it[index] = null }
        _assignedCards.value = updated
    }

    fun onDialogDismiss() {
        _showDialog.value = false
        _selectedSpot.value = null
    }

    fun onCardSelected(card: Card) {
        val spot = _selectedSpot.value ?: return
        val updated = _assignedCards.value.toMutableList().also { it[spot] = card }
        _assignedCards.value = updated
        _showDialog.value = false
        _selectedSpot.value = null
    }

    fun availableCards(): List<Card> {
        return Card.entries.filter { card -> _assignedCards.value.none { it == card } }
            .sortedBy { it.newDeckOrderIndex }
    }

    fun onResetStack() {
        viewModelScope.launch(Dispatchers.IO) {
            stackDao.clearAll()
            _assignedCards.value = List(spotCount) { null }
        }
    }
}
