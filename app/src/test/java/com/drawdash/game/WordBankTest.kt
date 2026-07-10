package com.drawdash.game

import com.drawdash.game.data.WordBank
import com.drawdash.game.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordBankTest {
    @Test fun wordDatabaseHasRequiredSizeByDifficulty() {
        assertTrue(WordBank.words.count { it.difficulty == Difficulty.Easy } >= 150)
        assertTrue(WordBank.words.count { it.difficulty == Difficulty.Medium } >= 150)
        assertTrue(WordBank.words.count { it.difficulty == Difficulty.Hard } >= 100)
    }

    @Test fun choicesAvoidRepeatAndSimilarity() {
        val used = WordBank.words.take(20).map { it.id }.toSet()
        val choices = WordBank.choices(used, Difficulty.Hard, 42)
        assertEquals(2, choices.size)
        assertTrue(choices.none { it.id in used })
        assertNotEquals(choices[0].category, choices[1].category)
    }
}
