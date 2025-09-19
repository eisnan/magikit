package com.magikit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.magikit.ui.theme.MagikitTheme

enum class NotationType { CHSD, SYMBOL }

object CardSelectionHistory {
    private val _history = mutableListOf<String>()
    val history: List<String> get() = _history
    fun add(cardCode: String) {
        _history.add(cardCode)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagikitTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") {
                            MainMenu(
                                onShowAllCards = { navController.navigate("all_cards") },
                                onPickCard = { cardCode -> navController.navigate("picked_card/$cardCode") },
                                onShowStatistics = { navController.navigate("statistics") }
                            )
                        }
                        composable("all_cards") {
                            AllCardsScreen(onBack = { navController.popBackStack() })
                        }
                        composable(
                            "picked_card/{cardCode}",
                            arguments = listOf(navArgument("cardCode") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val cardCode = backStackEntry.arguments?.getString("cardCode")
                            PickedCardScreen(cardCode = cardCode, onBack = { navController.popBackStack() }, onShowStatistics = { navController.navigate("statistics") })
                        }
                        composable("statistics") {
                            StatisticsScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenu(
    modifier: Modifier = Modifier,
    onShowAllCards: () -> Unit = {},
    onPickCard: (String) -> Unit = {},
    onShowStatistics: () -> Unit = {}
) {
    val deck = remember { Deck() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val card = deck.pickRandomCard()
                    CardSelectionHistory.add(card.code)
                    onPickCard(card.code)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Pick a card")
            }
            Button(onClick = { onShowAllCards() }, modifier = Modifier.padding(8.dp)) {
                Text("Design your stack")
            }
            Button(onClick = { /* TODO */ }, modifier = Modifier.padding(8.dp)) {
                Text("Train your stack")
            }
        }
    }
}

@Composable
fun PickedCardScreen(cardCode: String?, onBack: () -> Unit, onShowStatistics: () -> Unit) {
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
                            CardSelectionHistory.add(newCard.code)
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

data class DraggableCard(val id: Int, val card: Card)

@Composable
fun AllCardsScreen(onBack: () -> Unit) {
    val initialCards = remember {
        mutableStateListOf<DraggableCard>().apply {
            var id = 0
            addAll(Card.entries.map { card ->
                DraggableCard(id++, card)
            })
        }
    }
    val cardSize = 80.dp

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Text("Back")
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
            itemsIndexed(initialCards) { index, draggableCard ->
                Box(
                    modifier = Modifier
                        .size(cardSize)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            (index + 1).toString(),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Image(
                            painter = painterResource(id = draggableCard.card.drawableRes),
                            contentDescription = draggableCard.card.code,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsScreen(onBack: () -> Unit) {
    var isBackPressed by remember { mutableStateOf(false) }
    // Count occurrences of each card code
    val cardCounts = remember {
        CardSelectionHistory.history.groupingBy { it }.eachCount()
    }
    val cardsWithCounts = Card.entries.map { card ->
        card to (cardCounts[card.code] ?: 0)
    }.filter { it.second > 0 }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Statistics", modifier = Modifier.padding(bottom = 16.dp))
        if (cardsWithCounts.isEmpty()) {
            Text("No cards have been picked yet.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(cardsWithCounts.sortedByDescending { it.second }) { (card, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = Deck.toSymbol(card),
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 35.sp
                        )
                        Text(
                            text = "$count times",
                            modifier = Modifier.padding(top = 2.dp),
                            fontSize = 35.sp
                        )
                    }
                }
            }
        }
        Button(
            onClick = {
                isBackPressed = true
                onBack()
            },
            enabled = !isBackPressed,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Back")
        }
    }
}
