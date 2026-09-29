package com.mratubhayas.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

data class ToneStep(val freq: Int, val durMs: Int)

object NokiaToneEngine {
    private const val SAMPLE_RATE = 22050
    private var currentJob: Job? = null
    private var isPlaying = false

    // Exact melodies replicated from the ESP32 MratuBhayas firmware
    val TONES = listOf(
        // 0: NOKIA INTRO (Grande Valse)
        listOf(
            ToneStep(1319, 120), ToneStep(1175, 120), ToneStep(740, 220), ToneStep(831, 220),
            ToneStep(1109, 120), ToneStep(988, 120), ToneStep(587, 220), ToneStep(659, 220),
            ToneStep(988, 120), ToneStep(880, 120), ToneStep(554, 220), ToneStep(659, 220),
            ToneStep(880, 480)
        ),
        // 1: SMS SPECIAL (Morse: ... -- ...)
        listOf(
            ToneStep(2093, 65), ToneStep(0, 55), ToneStep(2093, 65), ToneStep(0, 55),
            ToneStep(2093, 65), ToneStep(0, 180), ToneStep(2093, 180), ToneStep(0, 70),
            ToneStep(2093, 180), ToneStep(0, 180), ToneStep(2093, 65), ToneStep(0, 55),
            ToneStep(2093, 65)
        ),
        // 2: NOSTALGIA (Brahms Lullaby)
        listOf(
            ToneStep(523, 140), ToneStep(523, 140), ToneStep(659, 220), ToneStep(523, 140),
            ToneStep(659, 140), ToneStep(784, 360), ToneStep(698, 140), ToneStep(659, 140),
            ToneStep(587, 280)
        ),
        // 3: WILLIAM TELL (Rossini Gallop)
        listOf(
            ToneStep(659, 90), ToneStep(659, 90), ToneStep(659, 160), ToneStep(784, 160),
            ToneStep(1047, 160), ToneStep(784, 120), ToneStep(659, 120), ToneStep(784, 120),
            ToneStep(659, 160), ToneStep(523, 160), ToneStep(587, 120), ToneStep(659, 120),
            ToneStep(587, 120), ToneStep(523, 300)
        ),
        // 4: POLKA (Lively Accordion)
        listOf(
            ToneStep(880, 80), ToneStep(1047, 80), ToneStep(1319, 80), ToneStep(1760, 160),
            ToneStep(1568, 80), ToneStep(1319, 80), ToneStep(1047, 120), ToneStep(1175, 120),
            ToneStep(1319, 240)
        )
    )

    fun playTone(toneIdx: Int, loop: Boolean = false) {
        stop()
        isPlaying = true
        val steps = TONES.getOrElse(toneIdx) { TONES[0] }

        currentJob = CoroutineScope(Dispatchers.Default).launch {
            do {
                for (step in steps) {
                    if (!isPlaying) break
                    if (step.freq > 0) {
                        playPcmFreq(step.freq, step.durMs)
                    } else {
                        delay(step.durMs.toLong())
                    }
                }
                if (loop) delay(800)
            } while (loop && isPlaying)
        }
    }

    fun playBeep(freq: Int = 1800, durMs: Int = 80) {
        CoroutineScope(Dispatchers.Default).launch {
            playPcmFreq(freq, durMs)
        }
    }

    fun stop() {
        isPlaying = false
        currentJob?.cancel()
        currentJob = null
    }

    private fun playPcmFreq(freq: Int, durMs: Int) {
        val numSamples = (SAMPLE_RATE * durMs / 1000.0).toInt()
        val buffer = ShortArray(numSamples)

        val angularFreq = 2.0 * Math.PI * freq / SAMPLE_RATE
        for (i in 0 until numSamples) {
            // Generate clean sinusoidal wave with slight attack/decay envelope to prevent clicking
            val envelope = when {
                i < 100 -> i / 100.0
                i > numSamples - 100 -> (numSamples - i) / 100.0
                else -> 1.0
            }
            buffer[i] = (sin(i * angularFreq) * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        Thread.sleep(durMs.toLong())
        audioTrack.stop()
        audioTrack.release()
    }
}
