package com.drawdash.game.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.drawdash.game.model.Difficulty
import com.drawdash.game.model.UserPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("drawdash_prefs")

class PrefsRepository(private val context: Context) {
    private object Keys {
        val highScore = intPreferencesKey("high_score")
        val gamesPlayed = intPreferencesKey("games_played")
        val totalCorrect = intPreferencesKey("total_correct")
        val sound = booleanPreferencesKey("sound")
        val haptics = booleanPreferencesKey("haptics")
        val difficulty = intPreferencesKey("difficulty")
        val duration = intPreferencesKey("duration")
        fun best(seconds: Int) = intPreferencesKey("best_$seconds")
    }

    val prefs: Flow<UserPrefs> = context.dataStore.data.map { p ->
        val best = mapOf(30 to (p[Keys.best(30)] ?: 0), 60 to (p[Keys.best(60)] ?: 0), 90 to (p[Keys.best(90)] ?: 0), 120 to (p[Keys.best(120)] ?: 0))
        UserPrefs(
            highScore = p[Keys.highScore] ?: 0,
            gamesPlayed = p[Keys.gamesPlayed] ?: 0,
            totalCorrect = p[Keys.totalCorrect] ?: 0,
            bestByMode = best,
            sound = p[Keys.sound] ?: true,
            haptics = p[Keys.haptics] ?: true,
            difficulty = Difficulty.entries.getOrElse(p[Keys.difficulty] ?: 1) { Difficulty.Medium },
            duration = p[Keys.duration] ?: 60,
        )
    }

    suspend fun saveSettings(sound: Boolean, haptics: Boolean, difficulty: Difficulty, duration: Int) {
        context.dataStore.edit {
            it[Keys.sound] = sound
            it[Keys.haptics] = haptics
            it[Keys.difficulty] = difficulty.ordinal
            it[Keys.duration] = duration
        }
    }

    suspend fun recordMatch(duration: Int, score: Int, correct: Int) {
        context.dataStore.edit {
            it[Keys.gamesPlayed] = (it[Keys.gamesPlayed] ?: 0) + 1
            it[Keys.totalCorrect] = (it[Keys.totalCorrect] ?: 0) + correct
            it[Keys.highScore] = maxOf(it[Keys.highScore] ?: 0, score)
            val bestKey = Keys.best(duration)
            it[bestKey] = maxOf(it[bestKey] ?: 0, score)
        }
    }
}
