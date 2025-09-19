package com.magikit

import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.magikit.data.CardSelectionStat
import com.magikit.data.CardSelectionStatDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PickedCardScreen(
    cardCode: String?,
    onBack: () -> Unit,
    onShowStatistics: () -> Unit,
    statDao: CardSelectionStatDao
) {
    val deck = remember { Deck() }
    var currentCardCode by remember { mutableStateOf(cardCode) }
    var notation by remember { mutableStateOf(NotationType.SYMBOL) }
    val card = Card.entries.find { it.code == currentCardCode }
    val displayCard = card?.let {
        when (notation) {
            NotationType.CHSD -> it.code
            NotationType.SYMBOL -> Deck.toSymbol(it)
        }
    } ?: "Unknown"
    val activity = LocalContext.current as ComponentActivity

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                    Text("Back")
                }
                Button(onClick = {
                    notation = if (notation == NotationType.CHSD) NotationType.SYMBOL else NotationType.CHSD
                }, modifier = Modifier.padding(8.dp)) {
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
                            val newCard = deck.pickRandomCard()
                            activity.lifecycleScope.launch(Dispatchers.IO) {
                                val stat = statDao.getStat(newCard.code)
                                if (stat == null) {
                                    statDao.insert(CardSelectionStat(newCard.code, 1))
                                } else {
                                    statDao.incrementCount(newCard.code)
                                }
                            }
                            currentCardCode = newCard.code
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

