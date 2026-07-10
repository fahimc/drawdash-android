package com.drawdash.game

import com.drawdash.game.drawing.DrawingEngine
import com.drawdash.game.model.DrawingState
import com.drawdash.game.model.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Test

class DrawingEngineTest {
    @Test fun undoAndRedoRestoreStroke() {
        val engine = DrawingEngine()
        var state = DrawingState()
        val builder = engine.beginStroke(state, StrokePoint(0f, 0f, 1))
        engine.addPoint(builder, StrokePoint(20f, 20f, 2))
        state = engine.endStroke(state, builder)
        assertEquals(1, state.strokes.size)
        state = engine.undo(state)
        assertEquals(0, state.strokes.size)
        state = engine.redo(state)
        assertEquals(1, state.strokes.size)
    }
}
