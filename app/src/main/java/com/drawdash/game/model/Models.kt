package com.drawdash.game.model

import androidx.compose.ui.graphics.Color

enum class Difficulty { Easy, Medium, Hard }
enum class GamePhase { Idle, Countdown, ChoosingWord, Drawing, Recognising, Correct, Skipping, Finished, Paused }
enum class ToolType { Pencil, Eraser }

data class WordEntry(
    val id: String,
    val displayName: String,
    val category: String,
    val difficulty: Difficulty,
    val aliases: List<String> = emptyList(),
    val recognitionLabels: List<String> = listOf(displayName.lowercase()),
)

data class MatchMode(val seconds: Int) {
    companion object {
        val all = listOf(30, 60, 90, 120).map(::MatchMode)
    }
}

data class Competitor(
    val id: String,
    val name: String,
    val avatar: String,
    val isPlayer: Boolean,
    val skill: Float,
    val errorRate: Float,
    val score: Int = 0,
    val progress: Float = 0f,
)

data class StrokePoint(val x: Float, val y: Float, val timeMillis: Long, val pressure: Float = 1f)

data class DrawingStroke(
    val points: List<StrokePoint>,
    val color: Color,
    val width: Float,
    val tool: ToolType,
)

data class DrawingState(
    val strokes: List<DrawingStroke> = emptyList(),
    val undone: List<DrawingStroke> = emptyList(),
    val currentTool: ToolType = ToolType.Pencil,
    val strokeWidth: Float = 10f,
    val color: Color = Color(0xFF24324B),
) {
    val hasMeaningfulStrokes: Boolean get() = strokes.any { it.points.size > 3 && it.tool == ToolType.Pencil }
}

data class RecognitionResult(
    val suggestions: List<String>,
    val confidence: Float,
    val matched: Boolean,
    val message: String,
)

data class MatchStats(
    val correct: Int = 0,
    val skipped: Int = 0,
    val rounds: Int = 0,
    val completedWords: List<String> = emptyList(),
    val bestDrawing: List<DrawingStroke> = emptyList(),
)

data class GameState(
    val phase: GamePhase = GamePhase.Idle,
    val modeSeconds: Int = 60,
    val difficulty: Difficulty = Difficulty.Medium,
    val remainingSeconds: Int = 60,
    val countdownText: String = "",
    val competitors: List<Competitor> = emptyList(),
    val wordChoices: List<WordEntry> = emptyList(),
    val currentWord: WordEntry? = null,
    val drawing: DrawingState = DrawingState(),
    val recognition: RecognitionResult = RecognitionResult(emptyList(), 0f, false, "Pick one to draw"),
    val stats: MatchStats = MatchStats(),
    val skipsLeft: Int = 3,
    val pausedFrom: GamePhase? = null,
) {
    val playerScore: Int get() = competitors.firstOrNull { it.isPlayer }?.score ?: 0
}

data class UserPrefs(
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    val totalCorrect: Int = 0,
    val bestByMode: Map<Int, Int> = emptyMap(),
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val difficulty: Difficulty = Difficulty.Medium,
    val duration: Int = 60,
)
