package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MetronomeAudioEngine
import com.example.model.MetronomeLoop
import com.example.model.PracticeStatus
import com.example.model.SoundKit
import com.example.model.Subdivision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MetronomeUiState(
    // Playback state
    val practiceStatus: PracticeStatus = PracticeStatus.STOPPED,
    val practiceLoops: List<MetronomeLoop> = emptyList(),
    val currentLoopIndex: Int = 0,
    val currentBeatInLoop: Int = 0,
    val currentSubdivisionTick: Int = 0,

    // Mustard Dots Feedback
    val isCountInActive: Boolean = false,
    val countInRemaining: Int = 0,
    val totalCountInBeats: Int = 4,
    val activeDotIndex: Int = 0,
    val totalDotsInBar: Int = 4,
    val visualFlashKey: Long = 0L,

    // Global Settings
    val initialCountInEnabled: Boolean = true,
    val initialCountInBeats: Int = 2,
    val soundKit: SoundKit = SoundKit.WOODBLOCK,

    // Loop Editor State (unified compact section)
    val editingLoopId: String? = null,
    val editorBpm: Int = 110,
    val editorBeatsPerBar: Int = 4,
    val editorSubdivision: Subdivision = Subdivision.QUARTER,
    val editorTotalBeats: Int = 2,
    val editorHasCountIn: Boolean = true,
    val editorCountInBeats: Int = 2
)

class MetronomiconViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("metronomicon_prefs", Context.MODE_PRIVATE)
    private val audioEngine = MetronomeAudioEngine()

    private val _uiState: MutableStateFlow<MetronomeUiState>

    init {
        val savedKitKey = prefs.getString("selected_sound_kit", SoundKit.WOODBLOCK.key)
        val initialKit = SoundKit.entries.find { it.key == savedKitKey } ?: SoundKit.WOODBLOCK
        audioEngine.setSoundKit(initialKit)

        _uiState = MutableStateFlow(
            MetronomeUiState(
                soundKit = initialKit,
                practiceLoops = emptyList(), // Practice list empty by default
                initialCountInBeats = 2,      // Initial count-in default 2
                editorTotalBeats = 2,         // Loop length default 2
                editorCountInBeats = 2        // Loop count-in default 2
            )
        )
    }

    val uiState: StateFlow<MetronomeUiState> get() = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private val tapTimestamps = mutableListOf<Long>()

    // --- Master Playback Controls: Start and Stop on the same button ---

    fun togglePractice() {
        val state = _uiState.value
        if (state.practiceStatus == PracticeStatus.PLAYING_LOOP ||
            state.practiceStatus == PracticeStatus.INITIAL_COUNT_IN ||
            state.practiceStatus == PracticeStatus.LOOP_COUNT_IN
        ) {
            stopPractice()
        } else {
            startPractice()
        }
    }

    fun startPractice() {
        val loops = _uiState.value.practiceLoops
        if (loops.isEmpty()) return

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch(Dispatchers.Default) {
            runPracticeLoop()
        }
    }

    fun stopPractice() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update {
            it.copy(
                practiceStatus = PracticeStatus.STOPPED,
                currentLoopIndex = 0,
                currentBeatInLoop = 0,
                currentSubdivisionTick = 0,
                isCountInActive = false,
                activeDotIndex = 0
            )
        }
    }

    fun jumpToLoop(index: Int) {
        val loops = _uiState.value.practiceLoops
        if (index in loops.indices) {
            _uiState.update {
                it.copy(
                    currentLoopIndex = index,
                    currentBeatInLoop = 0,
                    currentSubdivisionTick = 0
                )
            }
            if (_uiState.value.practiceStatus == PracticeStatus.PLAYING_LOOP ||
                _uiState.value.practiceStatus == PracticeStatus.LOOP_COUNT_IN ||
                _uiState.value.practiceStatus == PracticeStatus.INITIAL_COUNT_IN
            ) {
                playbackJob?.cancel()
                playbackJob = viewModelScope.launch(Dispatchers.Default) {
                    runPracticeFromCurrentLoop(index)
                }
            }
        }
    }

    // --- Metronome Loop Execution ---

    private suspend fun runPracticeLoop() {
        val initialCountIn = _uiState.value.initialCountInEnabled
        val initialBeats = _uiState.value.initialCountInBeats
        val firstLoop = _uiState.value.practiceLoops.getOrNull(_uiState.value.currentLoopIndex) ?: return

        if (initialCountIn && initialBeats > 0) {
            _uiState.update {
                it.copy(
                    practiceStatus = PracticeStatus.INITIAL_COUNT_IN,
                    isCountInActive = true,
                    totalCountInBeats = initialBeats,
                    countInRemaining = initialBeats
                )
            }
            playCountInBeats(firstLoop.bpm, initialBeats)
        }

        runPracticeFromCurrentLoop(_uiState.value.currentLoopIndex)
    }

    private suspend fun runPracticeFromCurrentLoop(startIndex: Int) {
        var loopIdx = startIndex

        while (true) {
            val loops = _uiState.value.practiceLoops
            if (loopIdx !in loops.indices) {
                if (loops.isNotEmpty()) {
                    loopIdx = 0
                } else {
                    stopPractice()
                    break
                }
            }

            val currentLoop = loops[loopIdx]
            _uiState.update {
                it.copy(
                    currentLoopIndex = loopIdx,
                    currentBeatInLoop = 0,
                    currentSubdivisionTick = 0,
                    totalDotsInBar = currentLoop.beatsPerBar
                )
            }

            // Loop-specific count-in
            if (currentLoop.hasCountIn && currentLoop.countInBeats > 0) {
                _uiState.update {
                    it.copy(
                        practiceStatus = PracticeStatus.LOOP_COUNT_IN,
                        isCountInActive = true,
                        totalCountInBeats = currentLoop.countInBeats,
                        countInRemaining = currentLoop.countInBeats
                    )
                }
                playCountInBeats(currentLoop.bpm, currentLoop.countInBeats)
            }

            // Play the loop beats & subdivisions
            _uiState.update {
                it.copy(
                    practiceStatus = PracticeStatus.PLAYING_LOOP,
                    isCountInActive = false,
                    currentBeatInLoop = 0,
                    currentSubdivisionTick = 0,
                    totalDotsInBar = currentLoop.beatsPerBar
                )
            }

            playLoopMain(currentLoop)

            // Continuously loop practice sequence
            loopIdx = (loopIdx + 1) % loops.size
        }
    }

    private suspend fun playCountInBeats(bpm: Int, beats: Int) {
        val beatIntervalNanos = 60_000_000_000L / bpm
        var nextTick = System.nanoTime()

        for (b in beats downTo 1) {
            val dotIdx = beats - b
            _uiState.update {
                it.copy(
                    countInRemaining = b,
                    activeDotIndex = dotIdx,
                    visualFlashKey = System.currentTimeMillis()
                )
            }
            audioEngine.playCountIn()

            nextTick += beatIntervalNanos
            val delayNanos = nextTick - System.nanoTime()
            if (delayNanos > 0) {
                val delayMs = delayNanos / 1_000_000L
                if (delayMs > 0) delay(delayMs)
            }
        }
    }

    private suspend fun playLoopMain(loop: MetronomeLoop) {
        val ticksPerBeat = loop.subdivision.ticksPerBeat
        val tickIntervalNanos = 60_000_000_000L / (loop.bpm * ticksPerBeat)
        val totalTicks = loop.totalBeats * ticksPerBeat
        var nextTick = System.nanoTime()

        for (tickIndex in 0 until totalTicks) {
            val beatIndex = tickIndex / ticksPerBeat
            val subIndex = tickIndex % ticksPerBeat
            val beatInBar = beatIndex % loop.beatsPerBar

            if (subIndex == 0) {
                audioEngine.playBeat()
            } else {
                audioEngine.playSubdivision()
            }

            _uiState.update {
                it.copy(
                    currentBeatInLoop = beatIndex,
                    currentSubdivisionTick = subIndex,
                    activeDotIndex = beatIndex,
                    visualFlashKey = System.currentTimeMillis()
                )
            }

            nextTick += tickIntervalNanos
            val delayNanos = nextTick - System.nanoTime()
            if (delayNanos > 0) {
                val delayMs = delayNanos / 1_000_000L
                if (delayMs > 0) delay(delayMs)
            }
        }
    }

    // --- Sound Kit with Preference Persistence ---

    fun setSoundKit(kit: SoundKit) {
        audioEngine.setSoundKit(kit)
        prefs.edit().putString("selected_sound_kit", kit.key).apply()
        _uiState.update { it.copy(soundKit = kit) }
    }

    // --- Initial Count-In Stepper ---

    fun setInitialCountInEnabled(enabled: Boolean) {
        _uiState.update { it.copy(initialCountInEnabled = enabled) }
    }

    fun adjustInitialCountInBeats(delta: Int) {
        _uiState.update {
            val newBeats = (it.initialCountInBeats + delta).coerceIn(1, 32)
            it.copy(initialCountInBeats = newBeats)
        }
    }

    // --- Tap Tempo ---

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.add(now)
        if (tapTimestamps.size > 5) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            if (now - tapTimestamps[tapTimestamps.size - 2] > 2500) {
                tapTimestamps.clear()
                tapTimestamps.add(now)
                return
            }
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avgInterval = intervals.average()
            if (avgInterval > 0) {
                val computedBpm = (60000.0 / avgInterval).toInt().coerceIn(40, 300)
                setEditorBpm(computedBpm)
            }
        }
    }

    // --- Loop Editor Methods & Steppers ---

    fun setEditorBpm(bpm: Int) {
        _uiState.update { it.copy(editorBpm = bpm.coerceIn(40, 300)) }
    }

    fun adjustEditorBpm(delta: Int) {
        _uiState.update { it.copy(editorBpm = (it.editorBpm + delta).coerceIn(40, 300)) }
    }

    fun adjustEditorBeatsPerBar(delta: Int) {
        _uiState.update {
            val newVal = (it.editorBeatsPerBar + delta).coerceIn(1, 16)
            it.copy(editorBeatsPerBar = newVal)
        }
    }

    fun setEditorSubdivision(sub: Subdivision) {
        _uiState.update { it.copy(editorSubdivision = sub) }
    }

    fun adjustEditorTotalBeats(delta: Int) {
        _uiState.update {
            val newVal = (it.editorTotalBeats + delta).coerceIn(1, 256)
            it.copy(editorTotalBeats = newVal)
        }
    }

    fun setEditorHasCountIn(hasCountIn: Boolean) {
        _uiState.update { it.copy(editorHasCountIn = hasCountIn) }
    }

    fun adjustEditorCountInBeats(delta: Int) {
        _uiState.update {
            val newVal = (it.editorCountInBeats + delta).coerceIn(1, 32)
            it.copy(editorCountInBeats = newVal)
        }
    }

    // Sequential loop naming: named sequentially starting from 1
    fun commitEditorLoop() {
        val state = _uiState.value
        val updatedList = state.practiceLoops.toMutableList()

        if (state.editingLoopId != null) {
            val idx = updatedList.indexOfFirst { it.id == state.editingLoopId }
            if (idx != -1) {
                val existing = updatedList[idx]
                updatedList[idx] = existing.copy(
                    bpm = state.editorBpm,
                    beatsPerBar = state.editorBeatsPerBar,
                    subdivision = state.editorSubdivision,
                    totalBeats = state.editorTotalBeats,
                    hasCountIn = state.editorHasCountIn,
                    countInBeats = state.editorCountInBeats
                )
            }
        } else {
            val loopIndex = updatedList.size + 1
            val loop = MetronomeLoop(
                id = UUID.randomUUID().toString(),
                name = "Loop $loopIndex",
                bpm = state.editorBpm,
                beatsPerBar = state.editorBeatsPerBar,
                subdivision = state.editorSubdivision,
                totalBeats = state.editorTotalBeats,
                hasCountIn = state.editorHasCountIn,
                countInBeats = state.editorCountInBeats
            )
            updatedList.add(loop)
        }

        // Ensure loops are named sequentially 1..N
        val renumbered = updatedList.mapIndexed { i, l ->
            l.copy(name = "Loop ${i + 1}")
        }

        _uiState.update {
            it.copy(
                practiceLoops = renumbered,
                editingLoopId = null
            )
        }
    }

    fun editLoop(loop: MetronomeLoop) {
        _uiState.update {
            it.copy(
                editingLoopId = loop.id,
                editorBpm = loop.bpm,
                editorBeatsPerBar = loop.beatsPerBar,
                editorSubdivision = loop.subdivision,
                editorTotalBeats = loop.totalBeats,
                editorHasCountIn = loop.hasCountIn,
                editorCountInBeats = loop.countInBeats
            )
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(editingLoopId = null)
        }
    }

    fun deleteLoop(loopId: String) {
        _uiState.update { current ->
            val updated = current.practiceLoops.filterNot { it.id == loopId }
            val renumbered = updated.mapIndexed { i, l ->
                l.copy(name = "Loop ${i + 1}")
            }
            val newIndex = current.currentLoopIndex.coerceAtMost((renumbered.size - 1).coerceAtLeast(0))
            current.copy(
                practiceLoops = renumbered,
                currentLoopIndex = newIndex
            )
        }
    }

    fun moveLoopUp(index: Int) {
        if (index <= 0) return
        _uiState.update { current ->
            val list = current.practiceLoops.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            val renumbered = list.mapIndexed { i, l -> l.copy(name = "Loop ${i + 1}") }
            current.copy(practiceLoops = renumbered)
        }
    }

    fun moveLoopDown(index: Int) {
        val list = _uiState.value.practiceLoops
        if (index >= list.size - 1) return
        _uiState.update { current ->
            val updated = current.practiceLoops.toMutableList()
            val item = updated.removeAt(index)
            updated.add(index + 1, item)
            val renumbered = updated.mapIndexed { i, l -> l.copy(name = "Loop ${i + 1}") }
            current.copy(practiceLoops = renumbered)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        audioEngine.release()
    }
}
