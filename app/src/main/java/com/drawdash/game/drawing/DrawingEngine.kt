package com.drawdash.game.drawing

import androidx.compose.ui.graphics.Color
import com.drawdash.game.model.DrawingState
import com.drawdash.game.model.DrawingStroke
import com.drawdash.game.model.StrokePoint
import com.drawdash.game.model.ToolType
import kotlin.math.hypot

class DrawingEngine(private val maxHistory: Int = 80) {
    fun beginStroke(state: DrawingState, point: StrokePoint): Builder =
        Builder(state.currentTool, state.color, state.strokeWidth, mutableListOf(point))

    fun addPoint(builder: Builder, point: StrokePoint): Builder {
        val last = builder.points.lastOrNull()
        if (last == null || hypot(point.x - last.x, point.y - last.y) >= 2f) {
            builder.points += smooth(last, point)
        }
        return builder
    }

    fun endStroke(state: DrawingState, builder: Builder?): DrawingState {
        if (builder == null || builder.points.size < 2) return state
        val stroke = DrawingStroke(builder.points.toList(), builder.color, builder.width, builder.tool)
        return state.copy(strokes = (state.strokes + stroke).takeLast(maxHistory), undone = emptyList())
    }

    fun undo(state: DrawingState): DrawingState =
        if (state.strokes.isEmpty()) state else state.copy(
            strokes = state.strokes.dropLast(1),
            undone = (state.undone + state.strokes.last()).takeLast(maxHistory),
        )

    fun redo(state: DrawingState): DrawingState =
        if (state.undone.isEmpty()) state else state.copy(
            strokes = (state.strokes + state.undone.last()).takeLast(maxHistory),
            undone = state.undone.dropLast(1),
        )

    fun clear(state: DrawingState): DrawingState = state.copy(strokes = emptyList(), undone = emptyList())
    fun tool(state: DrawingState, tool: ToolType): DrawingState = state.copy(currentTool = tool)
    fun width(state: DrawingState, width: Float): DrawingState = state.copy(strokeWidth = width.coerceIn(4f, 32f))
    fun color(state: DrawingState, color: Color): DrawingState = state.copy(color = color, currentTool = ToolType.Pencil)

    private fun smooth(last: StrokePoint?, next: StrokePoint): StrokePoint {
        if (last == null) return next
        return next.copy(
            x = (last.x * 0.25f) + (next.x * 0.75f),
            y = (last.y * 0.25f) + (next.y * 0.75f),
            pressure = ((last.pressure + next.pressure) / 2f).coerceIn(0.1f, 1f),
        )
    }

    data class Builder(
        val tool: ToolType,
        val color: Color,
        val width: Float,
        val points: MutableList<StrokePoint>,
    )
}
