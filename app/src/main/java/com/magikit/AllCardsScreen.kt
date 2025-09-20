package com.magikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.magikit.data.AppDatabase
import com.magikit.data.StackPosition

@Composable
fun AllCardsScreen(onBack: () -> Unit) {
    val spotCount = 52
    val cardSize = 240.dp
    var showDialog by remember { mutableStateOf(false) }
    var selectedSpot by remember { mutableStateOf<Int?>(null) }
    var assignedCards by remember { mutableStateOf(List<Card?>(spotCount) { null }) }

    val context = LocalContext.current
    val saveScope = remember { CoroutineScope(Dispatchers.IO) }

    // Load stack from DB on first composition
    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        val stackDao = db.stackPositionDao()
        val savedStack = stackDao.getAll()
        if (savedStack.isNotEmpty()) {
            assignedCards = List(spotCount) { idx ->
                val pos = savedStack.find { it.position == idx }
                pos?.cardCode?.let { code -> Card.entries.find { it.code == code } }
            }
        }
    }

    // Cards not yet assigned to any spot
    val availableCards = remember(assignedCards) {
        Card.entries.filter { card -> assignedCards.none { it == card } }
            .sortedBy { it.newDeckOrderIndex }
    }

    val cellWidth = 120.dp
    val cellHeight = 120.dp
    val imageSize = 120.dp

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Back")
            }
            Button(
                onClick = {
                    // Randomly assign cards to all spots
                    val shuffled = Card.entries.shuffled()
                    assignedCards = shuffled.take(spotCount)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("I'm feeling lucky")
            }
            Button(
                onClick = {
                    saveScope.launch {
                        saveStackToDatabase(context, assignedCards)
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Save stack")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .padding(1.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(spotCount) { index ->
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .height(cellHeight)
                        .clickable {
                            selectedSpot = index
                            showDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            (index + 1).toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (assignedCards[index] != null) {
                            Image(
                                painter = painterResource(assignedCards[index]!!.drawableRes),
                                contentDescription = assignedCards[index]!!.code,
                                modifier = Modifier.size(imageSize)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(imageSize)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedSpot != null) {
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
                        items(availableCards) { card ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    assignedCards = assignedCards.toMutableList().also { it[selectedSpot!!] = card }
                                    showDialog = false
                                    selectedSpot = null
                                }
                            ) {
                                Image(
                                    painter = painterResource(card.drawableRes),
                                    contentDescription = card.code,
                                    modifier = Modifier.size(60.dp)
                                )
                                Text(
                                    card.newDeckOrderIndex.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showDialog = false; selectedSpot = null }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

suspend fun saveStackToDatabase(context: android.content.Context, cards: List<Card?>) {
    val db = AppDatabase.getInstance(context)
    val stackDao = db.stackPositionDao()
    stackDao.clearAll()
    val stack = cards.mapIndexed { idx, card ->
        StackPosition(position = idx, cardCode = card?.code)
    }
    stackDao.insertAll(stack)
}
