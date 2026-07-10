package com.drawdash.game

import com.drawdash.game.engine.OpponentEngine
import com.drawdash.game.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpponentEngineTest {
    @Test fun opponentsAreVariedAndDeterministic() {
        val a = OpponentEngine.createOpponents(Difficulty.Medium, 9)
        val b = OpponentEngine.createOpponents(Difficulty.Medium, 9)
        assertEquals(a.map { it.name }, b.map { it.name })
        assertEquals(3, a.map { it.name }.distinct().size)
    }

    @Test fun opponentEventuallyScoresWithoutIdenticalIntervals() {
        var opponents = OpponentEngine.createOpponents(Difficulty.Medium, 5)
        repeat(30) { second ->
            opponents = opponents.map { OpponentEngine.tick(it, second, Difficulty.Medium) }
        }
        assertTrue(opponents.any { it.score > 0 })
        assertTrue(opponents.map { it.score }.distinct().size > 1)
    }
}
