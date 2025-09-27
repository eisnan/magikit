package com.magikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TrainYourStackScreen(
    onBack: () -> Unit = {},
    viewModel: TrainYourStackViewModel = viewModel()
) {
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val showQuestion by viewModel.showQuestion.collectAsState()
    val answer by viewModel.answer.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val allCards by viewModel.allCards.collectAsState()
    val revealed by viewModel.revealed.collectAsState()
    val answered by viewModel.answered.collectAsState()

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
                val selectedCard = allCards.find { it.code == answer }
                val correctCard = allCards.find { it.code == cardCode }
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
                        val imageCard = if (revealed) correctCard else selectedCard
                        Image(
                            painter = painterResource(imageCard?.drawableRes ?: R.drawable.brb),
                            contentDescription = imageCard?.code ?: "",
                            modifier = Modifier.size(100.dp)
                                .combinedClickable(onClick = { viewModel.onImageClick() })
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!answered) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { viewModel.onCheck() }) {
                                Text("Check")
                            }
                            Button(onClick = { viewModel.onReveal() }) {
                                Text("Reveal")
                            }
                        }
                    } else {
                        Button(onClick = { viewModel.onNext() }) {
                            Text("Next")
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
        Dialog(onDismissRequest = { viewModel.onDialogDismiss() }) {
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
                                    viewModel.onCardSelect(card.code)
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
                    Button(onClick = { viewModel.onDialogDismiss() }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
