package com.drawdash.game

import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drawdash.game.engine.DrawDashViewModel
import com.drawdash.game.model.Competitor
import com.drawdash.game.model.Difficulty
import com.drawdash.game.model.DrawingStroke
import com.drawdash.game.model.GamePhase
import com.drawdash.game.model.GameState
import com.drawdash.game.model.ToolType
import com.drawdash.game.model.UserPrefs

@OptIn(ExperimentalLayoutApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<DrawDashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrawDashTheme {
                App(viewModel)
            }
        }
    }
}

private object DashColor {
    val Cyan = Color(0xFF13B9D6)
    val Magenta = Color(0xFFE7468A)
    val Yellow = Color(0xFFFFD447)
    val Navy = Color(0xFF24324B)
    val Paper = Color(0xFFFFF9ED)
    val Success = Color(0xFF22A66B)
    val Warning = Color(0xFFFF6B35)
    val Ink = Color(0xFF24324B)
}

@Composable
fun DrawDashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = DashColor.Cyan,
            secondary = DashColor.Magenta,
            tertiary = DashColor.Yellow,
            background = DashColor.Paper,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onSurface = DashColor.Navy,
        ),
        content = content,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App(vm: DrawDashViewModel) {
    val nav = rememberNavController()
    val game by vm.game.collectAsState()
    val prefs by vm.prefs.collectAsState()
    NavHost(nav, startDestination = "splash", modifier = Modifier.fillMaxSize()) {
        composable("splash") {
            SplashScreen { nav.navigate("home") { popUpTo("splash") { inclusive = true } } }
        }
        composable("home") { HomeScreen(nav, prefs) }
        composable("mode") { ModeScreen(nav, prefs, vm) }
        composable("difficulty/{seconds}") { backStack ->
            val seconds = backStack.arguments?.getString("seconds")?.toIntOrNull() ?: 60
            DifficultyScreen(nav, seconds, prefs, vm)
        }
        composable("game") { GameScreen(nav, game, prefs, vm) }
        composable("results") { ResultsScreen(nav, game, prefs, vm) }
        composable("how") { HowToScreen(nav) }
        composable("stats") { StatsScreen(nav, prefs) }
        composable("settings") { SettingsScreen(nav, prefs, vm) }
    }
    LaunchedEffect(game.phase) {
        if (game.phase == GamePhase.Finished) nav.navigate("results") { launchSingleTop = true }
    }
}

@Composable
fun SplashScreen(done: () -> Unit) {
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(900); done() }
    Surface(Modifier.fillMaxSize(), color = DashColor.Cyan) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Logo(Modifier.scale(1.3f))
            Text("Fast doodles. Quick wins.", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(nav: NavHostController, prefs: UserPrefs) {
    Surface(Modifier.fillMaxSize(), color = DashColor.Paper) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(12.dp)); Logo() }
            item { PencilMascot() }
            item {
                Button(
                    onClick = { nav.navigate("mode") },
                    modifier = Modifier.fillMaxWidth().height(68.dp).semantics { contentDescription = "Play" },
                    colors = ButtonDefaults.buttonColors(containerColor = DashColor.Magenta),
                    shape = RoundedCornerShape(24.dp),
                ) { Text("Play", fontSize = 26.sp, fontWeight = FontWeight.Black) }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallNav("How to Play", Modifier.weight(1f)) { nav.navigate("how") }
                    SmallNav("Statistics", Modifier.weight(1f)) { nav.navigate("stats") }
                    SmallNav("Settings", Modifier.weight(1f)) { nav.navigate("settings") }
                }
            }
            item {
                InfoCard("High Score", "${prefs.highScore}", "Best 60s: ${prefs.bestByMode[60] ?: 0}")
            }
            item { Text("v1.0.0", color = DashColor.Navy.copy(alpha = 0.65f), fontSize = 12.sp) }
        }
    }
}

@Composable
fun ModeScreen(nav: NavHostController, prefs: UserPrefs, vm: DrawDashViewModel) {
    MenuScaffold("Choose Mode", nav) {
        MatchModeButtons { seconds -> nav.navigate("difficulty/$seconds") }
        Text("Last played: ${prefs.duration} seconds", color = DashColor.Navy.copy(alpha = 0.7f))
    }
}

@Composable
fun DifficultyScreen(nav: NavHostController, seconds: Int, prefs: UserPrefs, vm: DrawDashViewModel) {
    MenuScaffold("Difficulty", nav) {
        Difficulty.entries.forEach { diff ->
            Button(
                onClick = {
                    vm.saveSettings(prefs.sound, prefs.haptics, diff, seconds)
                    vm.startMatch(seconds, diff)
                    nav.navigate("game")
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(18.dp),
            ) { Text(diff.name, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(nav: NavHostController, state: GameState, prefs: UserPrefs, vm: DrawDashViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    val tones = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 55) }
    DisposableEffect(Unit) { onDispose { tones.release() } }
    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.Correct) {
            if (prefs.haptics) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (prefs.sound) tones.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        }
    }
    LaunchedEffect(state.remainingSeconds) {
        if (state.remainingSeconds in 1..3) {
            if (prefs.haptics) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (prefs.sound) tones.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) vm.pause() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Surface(Modifier.fillMaxSize(), color = DashColor.Paper) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).navigationBarsPadding().padding(10.dp)) {
            val boxMaxWidth = maxWidth
            val landscape = boxMaxWidth > maxHeight
            if (state.phase == GamePhase.Countdown) Countdown(state.countdownText) else if (landscape) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GameInfoColumn(state, vm, Modifier.width(if (boxMaxWidth > 840.dp) 330.dp else 260.dp).fillMaxHeight())
                    CanvasColumn(state, vm, Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Scoreboard(state.competitors, state.remainingSeconds, Modifier.fillMaxWidth())
                    WordPanel(state, vm)
                    RecognitionCard(state)
                    DrawingCanvas(state, vm, Modifier.weight(1f).fillMaxWidth())
                    Toolbar(state, vm)
                    BottomStatus(state)
                }
            }
            if (state.phase == GamePhase.Paused) PauseOverlay(vm, nav)
        }
    }
}

@Composable
fun GameInfoColumn(state: GameState, vm: DrawDashViewModel, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Scoreboard(state.competitors, state.remainingSeconds, Modifier.fillMaxWidth())
        WordPanel(state, vm)
        RecognitionCard(state)
        BottomStatus(state)
        Button(onClick = { vm.pause() }, modifier = Modifier.fillMaxWidth()) { Text("Pause") }
    }
}

@Composable
fun CanvasColumn(state: GameState, vm: DrawDashViewModel, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DrawingCanvas(state, vm, Modifier.weight(1f).fillMaxWidth())
        Toolbar(state, vm)
    }
}

@Composable
fun Scoreboard(competitors: List<Competitor>, remaining: Int, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            competitors.forEach { c ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (c.isPlayer) DashColor.Yellow else Color.White),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(34.dp).background(DashColor.Cyan, CircleShape), contentAlignment = Alignment.Center) {
                            Text(c.avatar, color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Text(c.name, maxLines = 1, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${c.score}", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Box(Modifier.fillMaxWidth().height(5.dp).background(DashColor.Navy.copy(alpha = 0.12f), RoundedCornerShape(5.dp))) {
                            Box(Modifier.fillMaxWidth(c.progress.coerceIn(0f, 1f)).height(5.dp).background(DashColor.Magenta, RoundedCornerShape(5.dp)))
                        }
                    }
                }
            }
        }
        TimerPill(remaining)
    }
}

@Composable
fun TimerPill(remaining: Int) {
    val warn = remaining <= 15
    val critical = remaining <= 5
    val transition = rememberInfiniteTransition(label = "timer")
    val pulse by transition.animateFloat(1f, if (warn) 1.06f else 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "pulse")
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "${remaining.coerceAtLeast(0)}s",
            modifier = Modifier.scale(if (warn) pulse else 1f).background(if (critical) DashColor.Warning else DashColor.Navy, RoundedCornerShape(50)).padding(horizontal = 24.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordPanel(state: GameState, vm: DrawDashViewModel) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.phase == GamePhase.ChoosingWord) {
                Text("Pick one to draw", fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.wordChoices.forEach { word ->
                        Button(
                            onClick = { vm.chooseWord(word) },
                            modifier = Modifier.height(56.dp).semantics { contentDescription = "Draw ${word.displayName}" },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DashColor.Cyan),
                        ) { Text(word.displayName.uppercase(), fontWeight = FontWeight.Black) }
                    }
                }
            } else {
                Text("Draw: ${state.currentWord?.displayName?.uppercase() ?: ""}", fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text(state.currentWord?.category?.replaceFirstChar { it.uppercase() } ?: "", color = DashColor.Navy.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun RecognitionCard(state: GameState) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (state.recognition.matched) DashColor.Success else DashColor.Yellow), shape = RoundedCornerShape(14.dp)) {
        Text(state.recognition.message, modifier = Modifier.padding(10.dp), color = if (state.recognition.matched) Color.White else DashColor.Navy, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DrawingCanvas(state: GameState, vm: DrawDashViewModel, modifier: Modifier) {
    AndroidView(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(2.dp, DashColor.Navy.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .semantics { contentDescription = "Drawing canvas" },
        factory = { context ->
            LowLatencyDrawingView(context).apply {
                onBegin = { x, y, pressure -> vm.beginStroke(x, y, pressure) }
                onMove = { x, y, pressure -> vm.moveStroke(x, y, pressure) }
                onEnd = { vm.endStroke() }
            }
        },
        update = { view ->
            view.isDrawingEnabled = state.phase == GamePhase.Drawing
            view.tool = state.drawing.currentTool
            view.strokeColor = state.drawing.color.toArgb()
            view.strokeWidthPx = state.drawing.strokeWidth
            view.setCommittedStrokes(state.drawing.strokes)
        },
    )
}

private class LowLatencyDrawingView(context: android.content.Context) : View(context) {
    var onBegin: (Float, Float, Float) -> Unit = { _, _, _ -> }
    var onMove: (Float, Float, Float) -> Unit = { _, _, _ -> }
    var onEnd: () -> Unit = {}
    var isDrawingEnabled: Boolean = false
    var tool: ToolType = ToolType.Pencil
    var strokeColor: Int = DashColor.Ink.toArgb()
    var strokeWidthPx: Float = 10f

    private var committedStrokes: List<DrawingStroke> = emptyList()
    private val livePoints = ArrayList<PointF>(256)
    private var liveColor: Int = strokeColor
    private var liveWidth: Float = strokeWidthPx
    private var liveTool: ToolType = tool
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = AndroidColor.argb(32, 19, 185, 214)
    }

    init {
        setBackgroundColor(AndroidColor.WHITE)
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = "Drawing canvas"
    }

    fun setCommittedStrokes(strokes: List<DrawingStroke>) {
        if (committedStrokes !== strokes) {
            committedStrokes = strokes
            invalidate()
        }
    }

    override fun onDraw(canvas: AndroidCanvas) {
        super.onDraw(canvas)
        committedStrokes.forEach { drawCommittedStroke(canvas, it) }
        drawLiveStroke(canvas)
        canvas.drawRoundRect(2f, 2f, width - 2f, height - 2f, 18f, 18f, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                livePoints.clear()
                liveColor = strokeColor
                liveWidth = strokeWidthPx
                liveTool = tool
                addLivePoint(event.x, event.y)
                onBegin(event.x, event.y, event.pressure.coerceAtLeast(0.1f))
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.historySize) {
                    val x = event.getHistoricalX(i)
                    val y = event.getHistoricalY(i)
                    addLivePoint(x, y)
                    onMove(x, y, event.getHistoricalPressure(i).coerceAtLeast(0.1f))
                }
                addLivePoint(event.x, event.y)
                onMove(event.x, event.y, event.pressure.coerceAtLeast(0.1f))
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    addLivePoint(event.x, event.y)
                    onMove(event.x, event.y, event.pressure.coerceAtLeast(0.1f))
                    onEnd()
                }
                livePoints.clear()
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return true
    }

    private fun addLivePoint(x: Float, y: Float) {
        val last = livePoints.lastOrNull()
        if (last == null || kotlin.math.hypot((x - last.x).toDouble(), (y - last.y).toDouble()) >= 1.5) {
            livePoints.add(PointF(x.coerceIn(0f, width.toFloat()), y.coerceIn(0f, height.toFloat())))
        }
    }

    private fun drawCommittedStroke(canvas: AndroidCanvas, stroke: DrawingStroke) {
        paint.color = if (stroke.tool == ToolType.Eraser) AndroidColor.WHITE else stroke.color.toArgb()
        paint.strokeWidth = stroke.width
        stroke.points.zipWithNext().forEach { (a, b) ->
            canvas.drawLine(a.x, a.y, b.x, b.y, paint)
        }
    }

    private fun drawLiveStroke(canvas: AndroidCanvas) {
        if (livePoints.size < 2) return
        paint.color = if (liveTool == ToolType.Eraser) AndroidColor.WHITE else liveColor
        paint.strokeWidth = liveWidth
        livePoints.zipWithNext().forEach { (a, b) ->
            canvas.drawLine(a.x, a.y, b.x, b.y, paint)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Toolbar(state: GameState, vm: DrawDashViewModel) {
    var confirmClear by remember { mutableStateOf(false) }
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ToolButton("Pencil", state.drawing.currentTool == ToolType.Pencil) { vm.setTool(ToolType.Pencil) }
        ToolButton("Eraser", state.drawing.currentTool == ToolType.Eraser) { vm.setTool(ToolType.Eraser) }
        ToolButton("Undo", false) { vm.undo() }
        ToolButton("Redo", false) { vm.redo() }
        ToolButton("Clear", false) { if (state.drawing.hasMeaningfulStrokes) confirmClear = true else vm.clear() }
        ToolButton("Skip ${state.skipsLeft}", false) { vm.skip() }
        ToolButton("Done", false) { vm.manualComplete() }
    }
    Slider(value = state.drawing.strokeWidth, onValueChange = vm::setWidth, valueRange = 4f..32f, modifier = Modifier.fillMaxWidth().height(38.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(DashColor.Ink, DashColor.Cyan, DashColor.Magenta, DashColor.Warning, Color(0xFF38A169)).forEach { color ->
            Box(Modifier.size(34.dp).background(color, CircleShape).border(2.dp, Color.White, CircleShape).clickable { vm.setColor(color) })
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            confirmButton = { TextButton(onClick = { vm.clear(); confirmClear = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Keep") } },
            title = { Text("Clear drawing?") },
            text = { Text("This removes the current strokes.") },
        )
    }
}

@Composable
fun ToolButton(label: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) DashColor.Yellow else Color.White),
        shape = RoundedCornerShape(14.dp),
    ) { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
}

@Composable
fun BottomStatus(state: GameState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Round ${state.stats.rounds + 1}", fontWeight = FontWeight.Bold)
        Text("Score ${state.playerScore}", fontWeight = FontWeight.Black)
        Text("Skips ${state.skipsLeft}", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Countdown(text: String) {
    val scale by rememberInfiniteTransition(label = "countdown").animateFloat(0.85f, 1.15f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "countdownScale")
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, modifier = Modifier.scale(scale), fontSize = 58.sp, fontWeight = FontWeight.Black, color = DashColor.Magenta)
    }
}

@Composable
fun PauseOverlay(vm: DrawDashViewModel, nav: NavHostController) {
    Box(Modifier.fillMaxSize().background(DashColor.Navy.copy(alpha = 0.72f)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Game Paused", fontSize = 28.sp, fontWeight = FontWeight.Black)
                Button(onClick = vm::resume, modifier = Modifier.fillMaxWidth()) { Text("Resume") }
                OutlinedButton(onClick = vm::restart, modifier = Modifier.fillMaxWidth()) { Text("Restart Match") }
                TextButton(onClick = { nav.navigate("home") { popUpTo("home") { inclusive = true } } }) { Text("Exit to Home") }
            }
        }
    }
}

@Composable
fun ResultsScreen(nav: NavHostController, state: GameState, prefs: UserPrefs, vm: DrawDashViewModel) {
    val ranking = state.competitors.sortedByDescending { it.score }
    MenuScaffold("Results", nav, showBack = false) {
        Text(if (state.playerScore >= prefs.highScore) "New high score!" else "Final ranking", fontWeight = FontWeight.Black, color = DashColor.Magenta)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            ranking.take(3).forEachIndexed { index, c ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(c.name, fontWeight = FontWeight.Bold)
                    Text("${c.score}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Box(Modifier.width(72.dp).height((90 - index * 18).dp).background(if (c.isPlayer) DashColor.Yellow else DashColor.Cyan, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Text("#${index + 1}", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        InfoCard("Your Match", "${state.playerScore} points", "${state.stats.correct} correct | ${state.stats.skipped} skipped | ${accuracy(state)} accuracy")
        Text("Words: ${state.stats.completedWords.take(8).joinToString().ifBlank { "None yet" }}")
        Button(onClick = { vm.startMatch(state.modeSeconds, state.difficulty); nav.navigate("game") }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Play Again") }
        OutlinedButton(onClick = { nav.navigate("home") { popUpTo("home") { inclusive = true } } }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Home") }
        OutlinedButton(onClick = { }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Share Result") }
    }
}

private fun accuracy(state: GameState): String {
    val attempts = (state.stats.correct + state.stats.skipped).coerceAtLeast(1)
    return "${state.stats.correct * 100 / attempts}%"
}

@Composable
fun HowToScreen(nav: NavHostController) {
    MenuScaffold("How to Play", nav) {
        listOf("Select one of two words.", "Draw it quickly.", "Wait for recognition.", "Complete as many as possible.", "Finish above the AI opponents.").forEachIndexed { i, line ->
            InfoCard("${i + 1}", line, "")
        }
        Text("Try the mini canvas", fontWeight = FontWeight.Black)
        MiniCanvas()
    }
}

@Composable
fun MiniCanvas() {
    var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
    Canvas(Modifier.fillMaxWidth().height(180.dp).background(Color.White, RoundedCornerShape(18.dp)).border(2.dp, DashColor.Cyan, RoundedCornerShape(18.dp)).pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val first = awaitPointerEvent().changes.firstOrNull { it.pressed } ?: continue
                val points = mutableListOf(first.position)
                while (first.pressed) {
                    val change = awaitPointerEvent().changes.first()
                    if (!change.pressed) break
                    points += change.position
                    change.consume()
                }
                strokes = strokes + listOf(points)
            }
        }
    }) {
        strokes.forEach { line -> line.zipWithNext().forEach { (a, b) -> drawLine(DashColor.Ink, a, b, 8f, StrokeCap.Round) } }
    }
}

@Composable
fun StatsScreen(nav: NavHostController, prefs: UserPrefs) {
    MenuScaffold("Statistics", nav) {
        InfoCard("High Score", "${prefs.highScore}", "Across all modes")
        InfoCard("Games Played", "${prefs.gamesPlayed}", "Total correct: ${prefs.totalCorrect}")
        prefs.bestByMode.forEach { (mode, score) -> InfoCard("${mode}s best", "$score", "") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(nav: NavHostController, prefs: UserPrefs, vm: DrawDashViewModel) {
    var sound by remember(prefs.sound) { mutableStateOf(prefs.sound) }
    var haptics by remember(prefs.haptics) { mutableStateOf(prefs.haptics) }
    var duration by remember(prefs.duration) { mutableStateOf(prefs.duration) }
    var difficulty by remember(prefs.difficulty) { mutableStateOf(prefs.difficulty) }
    MenuScaffold("Settings", nav) {
        SettingSwitch("Sound", sound) { sound = it }
        SettingSwitch("Haptics", haptics) { haptics = it }
        Text("Default duration", fontWeight = FontWeight.Bold)
        MatchModeButtons { duration = it }
        Text("Selected: ${duration}s")
        Text("Difficulty", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Difficulty.entries.forEach { diff -> ToolButton(diff.name, difficulty == diff) { difficulty = diff } }
        }
        Button(onClick = { vm.saveSettings(sound, haptics, difficulty, duration); nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
    }
}

@Composable
fun MenuScaffold(title: String, nav: NavHostController, showBack: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxSize(), color = DashColor.Paper) {
        LazyColumn(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (showBack) TextButton(onClick = { nav.popBackStack() }) { Text("Back") }
                    Text(title, fontSize = 30.sp, fontWeight = FontWeight.Black, color = DashColor.Navy)
                }
            }
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchModeButtons(onPick: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(30, 60, 90, 120).forEach { seconds ->
            Button(onClick = { onPick(seconds) }, modifier = Modifier.height(56.dp), shape = RoundedCornerShape(18.dp)) {
                Text("${seconds}s", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold)
        Switch(checked, onChange)
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("DrawDash", fontSize = 44.sp, fontWeight = FontWeight.Black, color = DashColor.Navy, letterSpacing = 0.sp)
        Box(Modifier.width(190.dp).height(8.dp).background(DashColor.Yellow, RoundedCornerShape(8.dp)))
    }
}

@Composable
fun PencilMascot() {
    val bob by rememberInfiniteTransition(label = "mascot").animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "bob")
    Box(Modifier.size(130.dp).scale(bob), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(DashColor.Yellow, topLeft = Offset(34f, 20f), size = androidx.compose.ui.geometry.Size(42f, 88f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f))
            drawCircle(DashColor.Magenta, radius = 18f, center = Offset(55f, 22f))
            drawLine(DashColor.Navy, Offset(42f, 102f), Offset(68f, 102f), 12f, StrokeCap.Round)
        }
    }
}

@Composable
fun SmallNav(text: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(16.dp)) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun InfoCard(title: String, value: String, detail: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = DashColor.Navy.copy(alpha = 0.65f), fontWeight = FontWeight.Bold)
            Text(value, color = DashColor.Navy, fontSize = 28.sp, fontWeight = FontWeight.Black)
            if (detail.isNotBlank()) Text(detail, color = DashColor.Navy.copy(alpha = 0.72f))
        }
    }
}
