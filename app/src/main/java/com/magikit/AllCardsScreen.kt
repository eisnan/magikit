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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AllCardsScreen(onBack: () -> Unit) {
    val spotCount = 52
    val cardSize = 80.dp
    var showDialog by remember { mutableStateOf(false) }
    var selectedSpot by remember { mutableStateOf<Int?>(null) }
    var assignedCards by remember { mutableStateOf(List<Card?>(spotCount) { null }) }

    // Cards not yet assigned to any spot
    val availableCards = remember(assignedCards) {
        Card.entries.filter { card -> assignedCards.none { it == card } }
            .sortedBy { it.newDeckOrderIndex }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Back")
            }
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("I'm feeling lucky")
            }
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Save stack")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(spotCount) { index ->
                Box(
                    modifier = Modifier
                        .size(cardSize)
                        .clickable {
                            selectedSpot = index
                            showDialog = true
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            (index + 1).toString(),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        if (assignedCards[index] != null) {
                            Image(
                                painter = painterResource(assignedCards[index]!!.drawableRes),
                                contentDescription = assignedCards[index]!!.code,
                                modifier = Modifier
                                    .size(cardSize / 2)
                                    .align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(cardSize / 2)
                                    .align(Alignment.CenterHorizontally)
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
