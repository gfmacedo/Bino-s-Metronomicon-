package com.example.model

import java.util.UUID

enum class Subdivision(
    val title: String,
    val symbol: String,
    val ticksPerBeat: Int
) {
    QUARTER("4th", "♩", 1),
    EIGHTH("8th", "♪♪", 2),
    SIXTEENTH("16th", "♬♬", 4),
    TRIPLET("3", "3", 3),
    SEXTUPLET("6", "6", 6)
}

enum class SoundKit(val displayName: String, val key: String) {
    WOODBLOCK("Studio Woodblock", "woodblock"),
    DIGITAL("Digital Pulse", "digital"),
    MECHANICAL("Mechanical Click", "mechanical"),
    RIMSHOT("Acoustic Rimshot", "rimshot"),
    COWBELL("80s Cowbell", "cowbell"),
    CLAVE("Latin Clave", "clave"),
    HIHAT("Crisp Hi-Hat", "hihat"),
    BELL("Metronome Bell", "bell"),
    BEEP("High Synth Beep", "beep")
}

enum class PracticeStatus {
    STOPPED,
    INITIAL_COUNT_IN,
    LOOP_COUNT_IN,
    PLAYING_LOOP,
    PAUSED
}

data class MetronomeLoop(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val bpm: Int = 120,
    val beatsPerBar: Int = 4,
    val beatUnit: Int = 4,
    val subdivision: Subdivision = Subdivision.QUARTER,
    val totalBeats: Int = 2,
    val hasCountIn: Boolean = true,
    val countInBeats: Int = 2
) {
    val totalBars: Float
        get() = totalBeats.toFloat() / beatsPerBar.toFloat()
}
