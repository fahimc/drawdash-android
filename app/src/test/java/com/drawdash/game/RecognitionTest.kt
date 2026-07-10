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
}
