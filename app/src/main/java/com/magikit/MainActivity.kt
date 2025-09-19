package com.magikit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.magikit.ui.theme.MagikitTheme

enum class NotationType { CHSD, SYMBOL }

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
                                onShowAllCards = { navController.navigate("all_cards") }
                            )
                        }
                        composable("all_cards") {
                            AllCardsScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainMenu(modifier: Modifier = Modifier, onShowAllCards: () -> Unit = {}) {
    val deck = remember { Deck() }
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    var notation by remember { mutableStateOf(NotationType.CHSD) }
    val notationOptions = listOf(NotationType.CHSD, NotationType.SYMBOL)
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Dropdown at top right
        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)) {
            Button(onClick = { expanded = true }) {
                Text(
                    when (notation) {
                        NotationType.CHSD -> "CHSD"
                        NotationType.SYMBOL -> "Symbol"
                    }
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                notationOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (option) {
                                    NotationType.CHSD -> "CHSD"
                                    NotationType.SYMBOL -> "Symbol"
                                }
                            )
                        },
                        onClick = {
                            notation = option
                            expanded = false
                        }
                    )
                }
            }
        }
        // Main content
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    selectedCard = deck.pickRandomCard()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Pick a card")
            }
            if (selectedCard != null) {
                val displayCard = when (notation) {
                    NotationType.CHSD -> selectedCard!!.code
                    NotationType.SYMBOL -> Deck.toSymbol(selectedCard!!)
                }
                Text("You picked: $displayCard", modifier = Modifier.padding(top = 24.dp))
                Image(
                    painter = painterResource(id = selectedCard!!.drawableRes),
                    contentDescription = displayCard,
                    modifier = Modifier.padding(top = 16.dp)
                )
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
