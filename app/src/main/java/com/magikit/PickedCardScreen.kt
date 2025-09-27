package com.magikit

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
fun PickedCardScreen(
    cardCode: String?,
    onShowStatistics: () -> Unit
) {
    val viewModel: PickedCardViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
                PickedCardViewModel(app, cardCode)
            }
        }
    )
    val currentCardCode by viewModel.currentCardCode.collectAsState()
    val notation by viewModel.notation.collectAsState()
    val card = Card.entries.find { it.code == currentCardCode }
    val displayCard = card?.let {
        when (notation) {
            NotationType.CHSD -> it.code
            NotationType.SYMBOL -> Deck.toSymbol(it)
        }
    } ?: "Unknown"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { viewModel.toggleNotation() }, modifier = Modifier.padding(8.dp)) {
                    Text(
                        when (notation) {
                            NotationType.CHSD -> "CHSD"
                            NotationType.SYMBOL -> "Symbol"
                        }
                    )
                }
            }
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (card != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            viewModel.pickRandomCardAndUpdateStat()
                        }
                    ) {
                        Text("You picked: $displayCard", modifier = Modifier.padding(top = 24.dp))
                        Image(
                            painter = painterResource(id = card.drawableRes),
                            contentDescription = displayCard,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text("Click on the card to cut to a new card", modifier = Modifier.padding(top = 24.dp))
                    }
                } else {
                    Text("Card not found", modifier = Modifier.padding(24.dp))
                }
            }
        }
        Button(
            onClick = onShowStatistics,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Statistics")
        }
    }
}
