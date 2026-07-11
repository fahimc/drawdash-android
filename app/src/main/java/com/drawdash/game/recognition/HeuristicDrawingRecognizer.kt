package com.drawdash.game.recognition

import com.drawdash.game.model.DrawingStroke
import com.drawdash.game.model.RecognitionResult
import com.drawdash.game.model.StrokePoint
import com.drawdash.game.model.ToolType
import com.drawdash.game.model.WordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class HeuristicDrawingRecognizer : DrawingRecognizer {
    override suspend fun initialise() = Unit
    override fun reset() = Unit

    override suspend fun recognise(strokes: List<DrawingStroke>, expectedWord: WordEntry): RecognitionResult =
        withContext(Dispatchers.Default) {
            delay(90)
            val features = Features.from(strokes.filter { it.tool == ToolType.Pencil })
            val suggestions = labelsFor(features, expectedWord.category)
            val expectedLabels = (expectedWord.recognitionLabels + expectedWord.aliases + expectedWord.displayName)
                .map { normalise(it) }
            val labelMatched = suggestions.map(::normalise).any { suggestion ->
                expectedLabels.any { label -> label == suggestion || label in suggestion || suggestion in label }
            }
            val categoryMatched = categoryMatch(expectedWord.category, features, expectedWord.difficulty.ordinal)
            val matched = labelMatched || categoryMatched
            val confidence = when {
                labelMatched -> 0.9f
                categoryMatched -> 0.82f
                else -> (features.inkRatio * 0.55f + features.effortScore * 0.35f).coerceIn(0.08f, 0.68f)
            }
            val message = when {
                matched -> "Correct!"
                suggestions.isEmpty() -> "I'm thinking..."
                confidence > 0.45f -> "Maybe a ${suggestions.first()}?"
                else -> "Almost there..."
            }
            RecognitionResult(suggestions, confidence, matched, message)
        }

    private fun labelsFor(f: Features, expectedCategory: String): List<String> = buildList {
        if (f.pointCount < 8) return@buildList
        if (f.closed && f.aspect in 0.75f..1.33f && f.cornerScore < 5) add("circle")
        if (f.closed && f.aspect in 0.75f..1.35f && f.cornerScore >= 4) add("square")
        if (f.closed && f.cornerScore in 2..4) add("triangle")
        if (f.aspect > 2.8f && !f.closed) add("line")
        if (f.aspect < 0.45f && f.strokeCount <= 3) add("tree")
        if (f.strokeCount >= 3 && f.aspect in 0.6f..1.8f) add("flower")
        if (f.strokeCount >= 4 && f.cornerScore > 8) add("star")
        if (f.closed && f.aspect in 1.4f..2.8f) add("boat")
        if (f.strokeCount >= 2 && f.aspect > 1.4f) add("car")
        if (f.closed && f.pointCount > 45 && f.aspect in 0.55f..1.8f) add("apple")
        if (f.strokeCount >= 2 && f.aspect < 0.9f) add("rocket")
        if (f.inkRatio > 0.22f && f.aspect in 0.7f..1.4f) add("face")
        if (categoryMatch(expectedCategory, f, 0)) add(expectedCategory)
    }.distinct().take(4)

    private fun categoryMatch(category: String, f: Features, difficultyOrdinal: Int): Boolean {
        val enoughInk = f.hasMeaningfulInk(difficultyOrdinal)
        return when (category) {
            "shape" -> f.closed || f.cornerScore >= 3 || (f.pointCount > 24 && f.majorSize > 36f)
            "nature" -> enoughInk && (f.strokeCount >= 2 || f.closed || f.cornerScore >= 2)
            "transport" -> enoughInk && (f.aspect > 1.15f || f.strokeCount >= 3)
            "animal" -> enoughInk && f.strokeCount >= 2 && f.aspect in 0.35f..3.2f
            "food" -> enoughInk && (f.closed || f.aspect in 0.45f..2.4f)
            "household" -> enoughInk && (f.cornerScore >= 1 || f.strokeCount >= 2)
            "object" -> enoughInk
            "clothing" -> enoughInk && f.aspect in 0.35f..3.4f
            "buildings" -> enoughInk && (f.cornerScore >= 2 || f.aspect in 0.45f..1.8f)
            "sports" -> enoughInk && (f.closed || f.strokeCount >= 2)
            "toys" -> enoughInk && (f.strokeCount >= 2 || f.closed)
            else -> enoughInk
        }
    }

    private fun Features.hasMeaningfulInk(difficultyOrdinal: Int): Boolean {
        val requiredPoints = when (difficultyOrdinal) {
            0 -> 18
            1 -> 18
            else -> 26
        }
        val requiredEffort = when (difficultyOrdinal) {
            0 -> 0.34f
            1 -> 0.36f
            else -> 0.44f
        }
        return pointCount >= requiredPoints &&
            majorSize >= 42f &&
            minorSize >= 24f &&
            effortScore >= requiredEffort
    }

    private fun categoryMatch(category: String, f: Features): Boolean = when (category) {
        "shape" -> f.closed || f.cornerScore >= 3
        "nature" -> f.strokeCount >= 2 && f.pointCount > 30
        "transport" -> f.aspect > 1.25f && f.strokeCount >= 2
        else -> false
    }

    private fun normalise(value: String) = value.lowercase()
        .replace("-", " ")
        .replace("_", " ")
        .trim()
        .removeSuffix("s")

    data class Features(
        val pointCount: Int,
        val strokeCount: Int,
        val aspect: Float,
        val closed: Boolean,
        val cornerScore: Int,
        val inkRatio: Float,
        val majorSize: Float,
        val minorSize: Float,
        val effortScore: Float,
    ) {
        companion object {
            fun from(strokes: List<DrawingStroke>): Features {
                val points = strokes.flatMap { it.points }
                if (points.isEmpty()) return Features(0, 0, 1f, false, 0, 0f, 0f, 0f, 0f)
                val minX = points.minOf { it.x }
                val maxX = points.maxOf { it.x }
                val minY = points.minOf { it.y }
                val maxY = points.maxOf { it.y }
                val w = max(1f, maxX - minX)
                val h = max(1f, maxY - minY)
                val major = max(w, h)
                val minor = min(w, h)
                val length = strokes.sumOf { stroke ->
                    stroke.points.zipWithNext().sumOf { (a, b) -> hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble()) }
                }.toFloat()
                val closed = strokes.any { stroke ->
                    val first = stroke.points.firstOrNull()
                    val last = stroke.points.lastOrNull()
                    first != null && last != null && hypot(last.x - first.x, last.y - first.y) < min(w, h) * 0.25f
                }
                val corners = strokes.sumOf { stroke ->
                    stroke.points.windowed(3, 5).count { (a, b, c) ->
                        val abx = b.x - a.x
                        val aby = b.y - a.y
                        val bcx = c.x - b.x
                        val bcy = c.y - b.y
                        abs(abx * bcy - aby * bcx) > 300f
                    }
                }
                val pointScore = (points.size / 80f).coerceAtMost(1f)
                val strokeScore = (strokes.size / 4f).coerceAtMost(1f)
                val sizeScore = (minor / 80f).coerceAtMost(1f)
                val geometryScore = when {
                    closed -> 0.18f
                    corners >= 2 -> 0.14f
                    else -> 0f
                }
                val effort = (pointScore * 0.42f + strokeScore * 0.24f + sizeScore * 0.2f + geometryScore).coerceIn(0f, 1f)
                return Features(points.size, strokes.size, w / h, closed, corners, length / (w * h).coerceAtLeast(1f), major, minor, effort)
            }
        }
    }
}
