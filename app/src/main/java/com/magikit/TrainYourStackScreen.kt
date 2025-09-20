package com.magikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.magikit.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import com.magikit.Card

@Composable
fun TrainYourStackScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var stack by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var currentQuestion by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showQuestion by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var allCards by remember { mutableStateOf(Card.entries.toList()) }

    // Load stack from DB on first composition
    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        val stackDao = db.stackPositionDao()
        val savedStack = withContext(Dispatchers.IO) { stackDao.getAll() }
        val pairs = savedStack.filter { it.cardCode != null }.map { it.position to it.cardCode!! }
        stack = pairs
        if (pairs.isNotEmpty()) {
            currentQuestion = pairs.random()
            showQuestion = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showQuestion && currentQuestion != null) {
                val (index, cardCode) = currentQuestion!!
                // Find the selected card based on the answer
                val selectedCard = allCards.find { it.code == answer }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Which card is at position ${index + 1}?",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(selectedCard?.drawableRes ?: R.drawable.brb),
                            contentDescription = selectedCard?.code ?: "",
                            modifier = Modifier.size(100.dp)
                                .combinedClickable( onClick = {
                                        showDialog = true
                                })
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            feedback = ""
                            if (answer.isNotEmpty()) {
                                if (answer == cardCode) {
                                    feedback = "Correct!"
                                } else {
                                    val correctCard = allCards.find { it.code == cardCode }
                                    feedback = "Incorrect. The correct card is ${correctCard?.code ?: cardCode}."
                                }
                            } else {
                                feedback = "Please select a card."
                            }
                        }) {
                            Text("Check")
                        }
                        Button(onClick = {
                            feedback = ""
                            if (stack.isNotEmpty()) {
                                currentQuestion = stack.random()
                                answer = ""
                            }
                        }) {
                            Text("Reveal")
                        }
                    }
                    if (feedback.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(feedback, fontSize = 18.sp)
                    }
                }
            } else {
                Text("No saved stack to train.", fontSize = 20.sp)
            }
        }
    }
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select a Card", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCards) { card ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    answer = card.code;
                                    showDialog = false
                                }
                            ) {
                                Image(
                                    painter = painterResource(card.drawableRes),
                                    contentDescription = card.code,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showDialog = false; }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
