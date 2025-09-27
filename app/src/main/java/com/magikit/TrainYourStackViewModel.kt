package com.magikit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magikit.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainYourStackViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val db = AppDatabase.getInstance(context)
    private val stackDao = db.stackPositionDao()

    private val _stack = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val stack: StateFlow<List<Pair<Int, String>>> = _stack.asStateFlow()

    private val _currentQuestion = MutableStateFlow<Pair<Int, String>?>(null)
    val currentQuestion: StateFlow<Pair<Int, String>?> = _currentQuestion.asStateFlow()

    private val _showQuestion = MutableStateFlow(false)
    val showQuestion: StateFlow<Boolean> = _showQuestion.asStateFlow()

    private val _answer = MutableStateFlow("")
    val answer: StateFlow<String> = _answer.asStateFlow()

    private val _feedback = MutableStateFlow("")
    val feedback: StateFlow<String> = _feedback.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _allCards = MutableStateFlow(Card.entries.toList())
    val allCards: StateFlow<List<Card>> = _allCards.asStateFlow()

    private val _revealed = MutableStateFlow(false)
    val revealed: StateFlow<Boolean> = _revealed.asStateFlow()

    private val _answered = MutableStateFlow(false)
    val answered: StateFlow<Boolean> = _answered.asStateFlow()

    init {
        loadStack()
    }

    private fun loadStack() {
        viewModelScope.launch {
            val savedStack = withContext(Dispatchers.IO) { stackDao.getAll() }
            val pairs = savedStack.filter { it.cardCode != null }.map { it.position to it.cardCode!! }
            _stack.value = pairs
            if (pairs.isNotEmpty()) {
                _currentQuestion.value = pairs.random()
                _showQuestion.value = true
            }
        }
    }

    fun onBack() {
        // Navigation handled by Composable
    }

    fun onImageClick() {
        _showDialog.value = true
        _revealed.value = false
    }

    fun onCardSelect(cardCode: String) {
        _answer.value = cardCode
        _showDialog.value = false
        _revealed.value = false
    }

    fun onCheck() {
        _feedback.value = ""
        val current = _currentQuestion.value
        val answer = _answer.value
        if (current != null && answer.isNotEmpty()) {
            val cardCode = current.second
            val correctCard = _allCards.value.find { it.code == cardCode }
            if (answer == cardCode) {
                _feedback.value = "Correct!"
            } else {
                _feedback.value = "Incorrect. The correct card is ${Deck.toSymbol(correctCard!!)}."
            }
            _answered.value = true
        } else {
            _feedback.value = "Please select a card."
        }
    }

    fun onReveal() {
        _feedback.value = ""
        val current = _currentQuestion.value
        if (current != null) {
            _answer.value = current.second
            _revealed.value = true
            _answered.value = true
        }
    }

    fun onNext() {
        _feedback.value = ""
        val pairs = _stack.value
        if (pairs.isNotEmpty()) {
            _currentQuestion.value = pairs.random()
        }
        _answer.value = ""
        _revealed.value = false
        _answered.value = false
    }

    fun onDialogDismiss() {
        _showDialog.value = false
    }

    fun resetForNewQuestion() {
        _answer.value = ""
        _revealed.value = false
        _answered.value = false
    }
}

