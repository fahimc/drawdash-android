package com.drawdash.game.recognition

import com.drawdash.game.model.DrawingStroke
import com.drawdash.game.model.RecognitionResult
import com.drawdash.game.model.WordEntry

interface DrawingRecognizer {
    suspend fun initialise()
    suspend fun recognise(strokes: List<DrawingStroke>, expectedWord: WordEntry): RecognitionResult
    fun reset()
}
