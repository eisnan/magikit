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

class PickedCardViewModel(application: Application, initialCardCode: String?) : AndroidViewModel(application) {
    private val statDao = AppDatabase.getInstance(application).cardSelectionStatDao()
    private val deck = Deck()

    private val _currentCardCode = MutableStateFlow(initialCardCode)
    val currentCardCode: StateFlow<String?> = _currentCardCode.asStateFlow()

    private val _notation = MutableStateFlow(NotationType.SYMBOL)
    val notation: StateFlow<NotationType> = _notation.asStateFlow()

    fun toggleNotation() {
        _notation.value = if (_notation.value == NotationType.CHSD) NotationType.SYMBOL else NotationType.CHSD
    }

    fun pickRandomCardAndUpdateStat() {
        val newCard = deck.pickRandomCard()
        viewModelScope.launch(Dispatchers.IO) {
            val stat = statDao.getStat(newCard.code)
            if (stat == null) {
                statDao.insert(CardSelectionStat(newCard.code, 1))
            } else {
                statDao.incrementCount(newCard.code)
            }
        }
        _currentCardCode.value = newCard.code
    }
}

