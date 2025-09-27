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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AllCardsScreen(
    viewModel: DesignStackViewModel = viewModel()
) {
    val spotCount = 52
    val showDialog by viewModel.showDialog.collectAsState()
    val selectedSpot by viewModel.selectedSpot.collectAsState()
    val assignedCards by viewModel.assignedCards.collectAsState()
    val availableCards = viewModel.availableCards()

    val cellWidth = 120.dp
    val cellHeight = 120.dp
    val imageSize = 120.dp

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { viewModel.onFeelingLucky() },
                modifier = Modifier.padding(4.dp)
            ) {
                Text("I'm feeling lucky")
            }
            Button(
                onClick = { viewModel.onResetStack() },
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Reset")
            }
            Button(
                onClick = { viewModel.onSaveStack() },
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Save")
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
                        .combinedClickable(
                            onClick = { viewModel.onCellClick(index) },
                            onLongClick = { viewModel.onCellLongClick(index) }
                        ),
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
                        items(availableCards) { card ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    viewModel.onCardSelected(card)
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
