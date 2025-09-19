package com.magikit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.magikit.data.AppDatabase
import com.magikit.data.CardSelectionStat
import com.magikit.data.CardSelectionStatDao
import com.magikit.ui.theme.MagikitTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class NotationType { CHSD, SYMBOL }

object CardSelectionHistory {
    private val _history = mutableListOf<String>()
    val history: List<String> get() = _history
    fun add(cardCode: String) {
        _history.add(cardCode)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var statDao: CardSelectionStatDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getInstance(applicationContext)
        statDao = db.cardSelectionStatDao()
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
                                onPickCard = { cardCode ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val stat = statDao.getStat(cardCode)
                                        if (stat == null) {
                                            statDao.insert(CardSelectionStat(cardCode, 1))
                                        } else {
                                            statDao.incrementCount(cardCode)
                                        }
                                    }
                                    navController.navigate("picked_card/$cardCode")
                                },
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
                            PickedCardScreen(
                                cardCode = cardCode,
                                onBack = { navController.popBackStack() },
                                onShowStatistics = { navController.navigate("statistics") },
                                statDao = statDao
                            )
                        }
                        composable("statistics") {
                            StatisticsScreen(onBack = { navController.popBackStack() }, statDao = statDao)
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
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pick a card tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable {
                        val card = deck.pickRandomCard()
                        CardSelectionHistory.add(card.code)
                        onPickCard(card.code)
                    },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pick a card")
                }
            }
            // Design your stack tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { onShowAllCards() },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Design your stack")
                }
            }
            // Train your stack tile
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { /* TODO: Implement training action */ },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Train your stack")
                }
            }
        }
    }
}
