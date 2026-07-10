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
            val suggestions = labelsFor(features)
            val expectedLabels = (expectedWord.recognitionLabels + expectedWord.aliases + expectedWord.displayName)
                .map { normalise(it) }
            val matched = suggestions.map(::normalise).any { suggestion ->
                expectedLabels.any { label -> label == suggestion || label in suggestion || suggestion in label }
            } || categoryMatch(expectedWord.category, features)
            val confidence = if (matched) 0.86f else (features.inkRatio * 0.55f).coerceIn(0.08f, 0.62f)
            val message = when {
                matched -> "Correct!"
                suggestions.isEmpty() -> "I'm thinking..."
                confidence > 0.45f -> "Maybe a ${suggestions.first()}?"
                else -> "Almost there..."
            }
            RecognitionResult(suggestions, confidence, matched, message)
        }

    private fun labelsFor(f: Features): List<String> = buildList {
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
    }.distinct().take(4)

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
    ) {
        companion object {
            fun from(strokes: List<DrawingStroke>): Features {
                val points = strokes.flatMap { it.points }
                if (points.isEmpty()) return Features(0, 0, 1f, false, 0, 0f)
                val minX = points.minOf { it.x }
                val maxX = points.maxOf { it.x }
                val minY = points.minOf { it.y }
                val maxY = points.maxOf { it.y }
                val w = max(1f, maxX - minX)
                val h = max(1f, maxY - minY)
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
                return Features(points.size, strokes.size, w / h, closed, corners, length / (w * h).coerceAtLeast(1f))
            }
        }
    }
}
