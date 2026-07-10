# Architecture

DrawDash uses a lightweight unidirectional state flow:

1. Compose screens render `GameState` and `UserPrefs`.
2. User actions call methods on `DrawDashViewModel`.
3. The view model updates the pure game state, schedules timers, and debounces recognition.
4. Drawing and recognition are delegated to separate classes.

Core packages:

- `model`: state, words, competitors, strokes, preferences
- `drawing`: history-safe stroke editing and smoothing
- `recognition`: recogniser interface and heuristic implementation
- `engine`: match loop, countdown, opponent scoring, pause/resume, completion
- `data`: DataStore preferences and structured word bank

The recogniser can be replaced by ML Kit or TensorFlow Lite by implementing `DrawingRecognizer`.
