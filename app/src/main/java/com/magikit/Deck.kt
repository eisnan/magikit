package com.magikit

import kotlin.random.Random

class Deck {
    private val suits = listOf("C", "H", "S", "D")
    private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    private val cards = suits.flatMap { suit -> ranks.map { rank -> "$rank$suit" } }

    fun pickRandomCard(): String {
        return cards[Random.nextInt(cards.size)]
    }

    companion object {
        fun toSymbol(card: String): String {
            if (card.length < 2) return card
            val rank = card.dropLast(1)
            val suitChar = card.last()
            val symbol = when (suitChar) {
                'C' -> '\u2663'
                'H' -> '\u2665'
                'S' -> '\u2660'
                'D' -> '\u2666'
                else -> suitChar
            }
            return "$rank$symbol"
        }
        // Returns the drawable resource name for a card (e.g., "h10" for Ten of Hearts, "sa" for Ace of Spades)
        fun getDrawableName(card: String): String {
            if (card.length < 2) return card.lowercase()
            val rank = card.dropLast(1).lowercase()
            val suit = card.last().lowercaseChar()
            return "$suit$rank"
        }
    }
}
