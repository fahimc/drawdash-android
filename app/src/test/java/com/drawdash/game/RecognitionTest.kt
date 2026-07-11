package com.drawdash.game

import androidx.compose.ui.graphics.Color
import com.drawdash.game.model.Difficulty
import com.drawdash.game.model.DrawingStroke
import com.drawdash.game.model.StrokePoint
import com.drawdash.game.model.ToolType
import com.drawdash.game.model.WordEntry
import com.drawdash.game.recognition.HeuristicDrawingRecognizer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class RecognitionTest {
    @Test fun recognisesCircleAndAliasLabels() = runTest {
        val points = (0..40).map {
            val angle = it / 40.0 * Math.PI * 2.0
            StrokePoint((50 + cos(angle) * 40).toFloat(), (50 + sin(angle) * 40).toFloat(), it.toLong())
        }
        val stroke = DrawingStroke(points, Color.Black, 10f, ToolType.Pencil)
        val result = HeuristicDrawingRecognizer().recognise(listOf(stroke), WordEntry("circle", "Circle", "shape", Difficulty.Easy, aliases = listOf("round")))
        assertTrue(result.matched)
    }

    @Test fun recognisesDrawableAnimalEffort() = runTest {
        val result = HeuristicDrawingRecognizer().recognise(
            strokes = sampleDrawing(),
            expectedWord = WordEntry("cat", "Cat", "animal", Difficulty.Easy),
        )
        assertTrue(result.matched)
    }

    @Test fun recognisesDrawableFoodEffort() = runTest {
        val result = HeuristicDrawingRecognizer().recognise(
            strokes = sampleDrawing(),
            expectedWord = WordEntry("pineapple", "Pineapple", "food", Difficulty.Medium),
        )
        assertTrue(result.matched)
    }

    @Test fun doesNotAcceptTinyAccidentalMark() = runTest {
        val tiny = listOf(DrawingStroke(listOf(StrokePoint(1f, 1f, 1), StrokePoint(4f, 4f, 2)), Color.Black, 10f, ToolType.Pencil))
        val result = HeuristicDrawingRecognizer().recognise(tiny, WordEntry("cat", "Cat", "animal", Difficulty.Easy))
        assertFalse(result.matched)
    }

    private fun sampleDrawing(): List<DrawingStroke> {
        fun stroke(vararg points: Pair<Float, Float>) = DrawingStroke(
            points.mapIndexed { index, point -> StrokePoint(point.first, point.second, index.toLong()) },
            Color.Black,
            10f,
            ToolType.Pencil,
        )
        return listOf(
            stroke(20f to 80f, 30f to 55f, 55f to 35f, 90f to 42f, 112f to 70f, 100f to 100f, 60f to 112f, 28f to 96f, 20f to 80f),
            stroke(42f to 58f, 49f to 50f, 56f to 58f),
            stroke(78f to 57f, 85f to 49f, 92f to 58f),
            stroke(45f to 82f, 70f to 86f, 96f to 82f),
        )
    }
}
