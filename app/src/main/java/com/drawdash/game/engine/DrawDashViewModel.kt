package com.drawdash.game.engine

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drawdash.game.data.PrefsRepository
import com.drawdash.game.data.WordBank
import com.drawdash.game.drawing.DrawingEngine
import com.drawdash.game.model.Competitor
import com.drawdash.game.model.Difficulty
import com.drawdash.game.model.DrawingState
import com.drawdash.game.model.GamePhase
import com.drawdash.game.model.GameState
import com.drawdash.game.model.MatchStats
import com.drawdash.game.model.StrokePoint
import com.drawdash.game.model.ToolType
import com.drawdash.game.model.UserPrefs
import com.drawdash.game.model.WordEntry
import com.drawdash.game.recognition.HeuristicDrawingRecognizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DrawDashViewModel(app: Application) : AndroidViewModel(app) {
    private val prefsRepo = PrefsRepository(app)
    private val recognizer = HeuristicDrawingRecognizer()
    private val drawingEngine = DrawingEngine()
    private val _game = MutableStateFlow(GameState())
    val game: StateFlow<GameState> = _game.asStateFlow()
    val prefs: StateFlow<UserPrefs> = prefsRepo.prefs.stateIn(viewModelScope, SharingStarted.Eagerly, UserPrefs())

    private var timerJob: Job? = null
    private var recognitionJob: Job? = null
    private var builder: DrawingEngine.Builder? = null
    private var usedWords = mutableSetOf<String>()
    private var seed = 7
    private var elapsed = 0

    init {
        viewModelScope.launch { recognizer.initialise() }
    }

    fun saveSettings(sound: Boolean, haptics: Boolean, difficulty: Difficulty, duration: Int) {
        viewModelScope.launch { prefsRepo.saveSettings(sound, haptics, difficulty, duration) }
    }

    fun startMatch(duration: Int = prefs.value.duration, difficulty: Difficulty = prefs.value.difficulty) {
        timerJob?.cancel()
        recognitionJob?.cancel()
        usedWords.clear()
        seed = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        elapsed = 0
        val competitors = listOf(Competitor("player", "Player", "YOU", true, 1f, 0f)) +
            OpponentEngine.createOpponents(difficulty, seed)
        _game.value = GameState(
            phase = GamePhase.Countdown,
            modeSeconds = duration,
            difficulty = difficulty,
            remainingSeconds = duration,
            competitors = competitors,
            countdownText = "3",
            skipsLeft = 3,
        )
        viewModelScope.launch {
            listOf("3", "2", "1", "Draw!").forEach {
                _game.value = _game.value.copy(countdownText = it)
                delay(650)
            }
            nextChoices()
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_game.value.remainingSeconds > 0 && _game.value.phase != GamePhase.Finished) {
                delay(1000)
                val state = _game.value
                if (state.phase == GamePhase.Paused || state.phase == GamePhase.Finished) continue
                elapsed++
                val competitors = state.competitors.map {
                    if (it.isPlayer) it else OpponentEngine.tick(it, elapsed, state.difficulty)
                }
                val remaining = (state.remainingSeconds - 1).coerceAtLeast(0)
                _game.value = state.copy(remainingSeconds = remaining, competitors = competitors)
                if (remaining == 0) finishMatch()
            }
        }
    }

    fun pause() {
        val phase = _game.value.phase
        if (phase != GamePhase.Finished && phase != GamePhase.Paused) {
            _game.value = _game.value.copy(phase = GamePhase.Paused, pausedFrom = phase)
        }
    }

    fun resume() {
        val previous = _game.value.pausedFrom ?: GamePhase.ChoosingWord
        _game.value = _game.value.copy(phase = previous, pausedFrom = null)
    }

    fun restart() = startMatch(_game.value.modeSeconds, _game.value.difficulty)

    fun nextChoices() {
        recognitionJob?.cancel()
        val choices = WordBank.choices(usedWords, _game.value.difficulty, ++seed)
        _game.value = _game.value.copy(
            phase = GamePhase.ChoosingWord,
            wordChoices = choices,
            currentWord = null,
            drawing = DrawingState(),
            recognition = _game.value.recognition.copy(suggestions = emptyList(), matched = false, message = "Pick one to draw"),
        )
    }

    fun chooseWord(word: WordEntry) {
        if (_game.value.phase != GamePhase.ChoosingWord) return
        usedWords += word.id
        _game.value = _game.value.copy(
            phase = GamePhase.Drawing,
            currentWord = word,
            wordChoices = emptyList(),
            recognition = _game.value.recognition.copy(message = "I'm thinking..."),
        )
    }

    fun beginStroke(x: Float, y: Float, pressure: Float) {
        if (_game.value.phase != GamePhase.Drawing) return
        builder = drawingEngine.beginStroke(_game.value.drawing, StrokePoint(x, y, System.currentTimeMillis(), pressure))
    }

    fun moveStroke(x: Float, y: Float, pressure: Float) {
        val b = builder ?: return
        drawingEngine.addPoint(b, StrokePoint(x, y, System.currentTimeMillis(), pressure))
    }

    fun endStroke() {
        if (_game.value.phase != GamePhase.Drawing) return
        _game.value = _game.value.copy(drawing = drawingEngine.endStroke(_game.value.drawing, builder))
        builder = null
        scheduleRecognition()
    }

    fun undo() = updateDrawing { drawingEngine.undo(it) }
    fun redo() = updateDrawing { drawingEngine.redo(it) }
    fun clear() = updateDrawing { drawingEngine.clear(it) }
    fun setTool(tool: ToolType) = updateDrawing { drawingEngine.tool(it, tool) }
    fun setWidth(width: Float) = updateDrawing { drawingEngine.width(it, width) }
    fun setColor(color: Color) = updateDrawing { drawingEngine.color(it, color) }

    fun skip() {
        val state = _game.value
        if (state.phase !in listOf(GamePhase.Drawing, GamePhase.ChoosingWord) || state.skipsLeft <= 0) return
        _game.value = state.copy(
            phase = GamePhase.Skipping,
            remainingSeconds = (state.remainingSeconds - 3).coerceAtLeast(0),
            skipsLeft = state.skipsLeft - 1,
            stats = state.stats.copy(skipped = state.stats.skipped + 1, rounds = state.stats.rounds + 1),
        )
        if (_game.value.remainingSeconds == 0) finishMatch() else viewModelScope.launch {
            delay(450)
            nextChoices()
        }
    }

    fun manualComplete() {
        if (_game.value.phase == GamePhase.Drawing) markCorrect()
    }

    private fun updateDrawing(block: (DrawingState) -> DrawingState) {
        val state = _game.value
        if (state.phase == GamePhase.Drawing) _game.value = state.copy(drawing = block(state.drawing))
    }

    private fun scheduleRecognition() {
        val word = _game.value.currentWord ?: return
        recognitionJob?.cancel()
        recognitionJob = viewModelScope.launch {
            delay(350)
            val state = _game.value
            if (state.phase != GamePhase.Drawing) return@launch
            _game.value = state.copy(phase = GamePhase.Recognising)
            val result = recognizer.recognise(state.drawing.strokes, word)
            val latest = _game.value
            if (latest.phase != GamePhase.Recognising && latest.phase != GamePhase.Drawing) return@launch
            _game.value = latest.copy(phase = GamePhase.Drawing, recognition = result)
            if (result.matched) markCorrect()
        }
    }

    private fun markCorrect() {
        val state = _game.value
        val word = state.currentWord ?: return
        val scored = state.competitors.map { if (it.isPlayer) it.copy(score = it.score + 1, progress = 0f) else it }
        _game.value = state.copy(
            phase = GamePhase.Correct,
            competitors = scored,
            recognition = state.recognition.copy(matched = true, message = "Correct!"),
            stats = state.stats.copy(
                correct = state.stats.correct + 1,
                rounds = state.stats.rounds + 1,
                completedWords = state.stats.completedWords + word.displayName,
                bestDrawing = if (state.drawing.strokes.size > state.stats.bestDrawing.size) state.drawing.strokes else state.stats.bestDrawing,
            ),
        )
        viewModelScope.launch {
            delay(700)
            if (_game.value.remainingSeconds > 0) nextChoices() else finishMatch()
        }
    }

    private fun finishMatch() {
        timerJob?.cancel()
        recognitionJob?.cancel()
        val state = _game.value
        if (state.phase == GamePhase.Finished) return
        _game.value = state.copy(phase = GamePhase.Finished, remainingSeconds = 0)
        viewModelScope.launch { prefsRepo.recordMatch(state.modeSeconds, state.playerScore, state.stats.correct) }
    }
}
