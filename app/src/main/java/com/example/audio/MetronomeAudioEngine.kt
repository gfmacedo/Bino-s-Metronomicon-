package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.model.SoundKit
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class MetronomeAudioEngine {
    private val sampleRate = 44100
    private var isMuted = false
    private var masterVolume = 0.9f

    private var soundKit: SoundKit = SoundKit.WOODBLOCK

    private var trackBeat: AudioTrack? = null
    private var trackSubdivision: AudioTrack? = null
    private var trackCountIn: AudioTrack? = null

    init {
        buildTracksForKit(soundKit)
    }

    fun setSoundKit(kit: SoundKit) {
        if (soundKit != kit) {
            soundKit = kit
            releaseTracks()
            buildTracksForKit(kit)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        applyVolume()
    }

    private fun applyVolume() {
        val vol = if (isMuted) 0f else masterVolume
        trackBeat?.setVolume(vol)
        trackSubdivision?.setVolume(vol)
        trackCountIn?.setVolume(vol)
    }

    private fun buildTracksForKit(kit: SoundKit) {
        trackBeat = createStaticTrack(generateBuffer(kit, SoundRole.BEAT))
        trackSubdivision = createStaticTrack(generateBuffer(kit, SoundRole.SUBDIVISION))
        trackCountIn = createStaticTrack(generateBuffer(kit, SoundRole.COUNT_IN))
        applyVolume()
    }

    fun playBeat() {
        if (isMuted) return
        triggerTrack(trackBeat)
    }

    fun playSubdivision() {
        if (isMuted) return
        triggerTrack(trackSubdivision)
    }

    fun playCountIn() {
        if (isMuted) return
        triggerTrack(trackCountIn)
    }

    private fun triggerTrack(track: AudioTrack?) {
        try {
            track?.let {
                it.stop()
                it.reloadStaticData()
                it.play()
            }
        } catch (_: Exception) {}
    }

    private fun createStaticTrack(pcmData: ShortArray): AudioTrack {
        val bufferSize = pcmData.size * 2
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val track = AudioTrack(
            attributes,
            format,
            bufferSize,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        track.write(pcmData, 0, pcmData.size)
        return track
    }

    private enum class SoundRole { BEAT, SUBDIVISION, COUNT_IN }

    private fun generateBuffer(kit: SoundKit, role: SoundRole): ShortArray {
        val durationMs = when (role) {
            SoundRole.BEAT -> 35
            SoundRole.SUBDIVISION -> 18
            SoundRole.COUNT_IN -> 45
        }
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples
            val decay = exp(-progress * 8.5)

            val sampleVal: Double = when (kit) {
                SoundKit.WOODBLOCK -> {
                    val baseFreq = when (role) {
                        SoundRole.BEAT -> 1150.0
                        SoundRole.SUBDIVISION -> 1450.0
                        SoundRole.COUNT_IN -> 1800.0
                    }
                    val f = baseFreq * (1.0 - progress * 0.3)
                    sin(2 * PI * f * t) * decay + 0.3 * sin(2 * PI * f * 2.05 * t) * decay
                }
                SoundKit.DIGITAL -> {
                    val f = when (role) {
                        SoundRole.BEAT -> 900.0
                        SoundRole.SUBDIVISION -> 1350.0
                        SoundRole.COUNT_IN -> 2000.0
                    }
                    sin(2 * PI * f * t) * decay
                }
                SoundKit.MECHANICAL -> {
                    val f = when (role) {
                        SoundRole.BEAT -> 1600.0
                        SoundRole.SUBDIVISION -> 1900.0
                        SoundRole.COUNT_IN -> 2400.0
                    }
                    val noise = ((i % 7) - 3.5) / 3.5 * 0.3
                    (sin(2 * PI * f * t) + noise) * decay
                }
                SoundKit.RIMSHOT -> {
                    val f = when (role) {
                        SoundRole.BEAT -> 520.0
                        SoundRole.SUBDIVISION -> 750.0
                        SoundRole.COUNT_IN -> 1100.0
                    }
                    val snap = if (i < 120) (Math.random() * 2.0 - 1.0) * 0.6 else 0.0
                    (sin(2 * PI * f * t) * 0.7 + snap) * decay
                }
                SoundKit.COWBELL -> {
                    val f1 = when (role) {
                        SoundRole.BEAT -> 580.0
                        SoundRole.SUBDIVISION -> 800.0
                        SoundRole.COUNT_IN -> 920.0
                    }
                    val f2 = f1 * 1.48
                    (sin(2 * PI * f1 * t) * 0.6 + sin(2 * PI * f2 * t) * 0.4) * exp(-progress * 6.0)
                }
                SoundKit.CLAVE -> {
                    val f = when (role) {
                        SoundRole.BEAT -> 2300.0
                        SoundRole.SUBDIVISION -> 2600.0
                        SoundRole.COUNT_IN -> 3000.0
                    }
                    sin(2 * PI * f * t) * exp(-progress * 14.0)
                }
                SoundKit.HIHAT -> {
                    val noise = (Math.random() * 2.0 - 1.0)
                    val tone = sin(2 * PI * 8000.0 * t) * 0.3
                    (noise * 0.7 + tone) * exp(-progress * 18.0)
                }
                SoundKit.BELL -> {
                    val f = when (role) {
                        SoundRole.BEAT -> 1200.0
                        SoundRole.SUBDIVISION -> 1600.0
                        SoundRole.COUNT_IN -> 2100.0
                    }
                    (sin(2 * PI * f * t) + 0.4 * sin(2 * PI * f * 2.76 * t)) * exp(-progress * 5.0)
                }
                SoundKit.BEEP -> {
                    val f = when (role) {
                        SoundRole.BEAT -> 1000.0
                        SoundRole.SUBDIVISION -> 1400.0
                        SoundRole.COUNT_IN -> 2200.0
                    }
                    val raw = sin(2 * PI * f * t)
                    (if (raw >= 0) 0.8 else -0.8) * decay
                }
            }

            val amplitude = when (role) {
                SoundRole.BEAT -> 0.9
                SoundRole.SUBDIVISION -> 0.45
                SoundRole.COUNT_IN -> 0.95
            }

            val pcm = (sampleVal * amplitude * 32767).coerceIn(-32768.0, 32767.0).toInt().toShort()
            buffer[i] = pcm
        }
        return buffer
    }

    fun release() {
        releaseTracks()
    }

    private fun releaseTracks() {
        try {
            trackBeat?.release()
            trackSubdivision?.release()
            trackCountIn?.release()
        } catch (_: Exception) {}
        trackBeat = null
        trackSubdivision = null
        trackCountIn = null
    }
}
