package com.drawdash.game.engine

import com.drawdash.game.model.Competitor
import com.drawdash.game.model.Difficulty
import kotlin.math.min
import kotlin.random.Random

object OpponentEngine {
    private val names = listOf("DoodleBot", "Sketchy", "Scribble", "PixelPen", "Artie", "Inkster")
    private val avatars = listOf("DB", "SK", "SC", "PP", "AR", "IN")

    fun createOpponents(difficulty: Difficulty, seed: Int): List<Competitor> {
        val random = Random(seed)
        return names.zip(avatars).shuffled(random).take(3).mapIndexed { index, (name, avatar) ->
            val base = when (difficulty) {
                Difficulty.Easy -> 0.42f
                Difficulty.Medium -> 0.62f
                Difficulty.Hard -> 0.78f
            }
            Competitor(
                id = "ai$index",
                name = name,
                avatar = avatar,
                isPlayer = false,
                skill = (base + random.nextFloat() * 0.16f - 0.08f).coerceIn(0.25f, 0.92f),
                errorRate = when (difficulty) {
                    Difficulty.Easy -> 0.34f
                    Difficulty.Medium -> 0.22f
                    Difficulty.Hard -> 0.13f
                },
            )
        }
    }

    fun tick(opponent: Competitor, elapsedSeconds: Int, difficulty: Difficulty): Competitor {
        val interval = when (difficulty) {
            Difficulty.Easy -> 12f - opponent.skill * 3f
            Difficulty.Medium -> 9f - opponent.skill * 3f
            Difficulty.Hard -> 7f - opponent.skill * 2.5f
        }.coerceAtLeast(3.5f)
        val progress = min(1f, opponent.progress + (1f / interval))
        val deterministicNoise = ((elapsedSeconds * 37 + opponent.name.length * 11) % 100) / 100f
        return if (progress >= 1f && deterministicNoise > opponent.errorRate) {
            opponent.copy(score = opponent.score + 1, progress = 0f)
        } else {
            opponent.copy(progress = if (progress >= 1f) 0.2f else progress)
        }
    }
}
