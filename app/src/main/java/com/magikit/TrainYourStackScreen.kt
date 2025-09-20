package com.magikit

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
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
import com.magikit.Card

@Composable
fun TrainYourStackScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var stack by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var currentQuestion by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showQuestion by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf(TextFieldValue("")) }
    var feedback by remember { mutableStateOf("") }

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
                            painter = painterResource(R.drawable.brb),
                            contentDescription = "",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            feedback = ""
                            answer = TextFieldValue("")
                            if (stack.isNotEmpty()) {
                                currentQuestion = stack.random()
                            }
                        }) {
                            Text("Skip")
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
}
