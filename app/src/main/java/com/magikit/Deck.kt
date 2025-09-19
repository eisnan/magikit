package com.magikit

import kotlin.random.Random

class Deck {
    private val cards = Card.entries.toList()

    fun pickRandomCard(): Card {
        return cards[Random.nextInt(cards.size)]
    }

    companion object {
        fun toSymbol(card: Card): String {
            val code = card.code
            val rank = code.dropLast(1)
            val suitChar = code.last()
            val symbol = when (suitChar.uppercaseChar()) {
                'C' -> '\u2663'
                'H' -> '\u2665'
                'S' -> '\u2660'
                'D' -> '\u2666'
                else -> suitChar
            }
            return "$rank$symbol"
        }
    }
}
