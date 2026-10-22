package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * High-performance, self-contained crazy & loud sound effects synthesizer and haptics engine.
 * Generates custom synthesized 16-bit PCM waveforms and loads them into Android's low-latency SoundPool.
 */
class CrazySoundEngine(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundType, Int>()
    private var isLoaded = false

    var volumeScale: Float = 1.25f // 0.0 to 1.5 (Crazy Boost!)
    var isCrazyModeEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    enum class SoundType {
        POP,
        DROP,
        TUBE_COMPLETE,
        LEVEL_WIN,
        COMBO_LASER,
        AIRHORN,
        ERROR_BOING,
        MAGIC_ADD,
        REWIND,
        CLICK,
        FIREWORKS
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        // Generate synthesized waveforms asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                synthesizeAllSounds()
            } catch (e: Exception) {
                Log.e("CrazySoundEngine", "Error synthesizing audio", e)
            }
        }
    }

    private fun synthesizeAllSounds() {
        val cacheDir = File(context.cacheDir, "crazy_sfx").apply { mkdirs() }

        loadSound(SoundType.POP, generatePopSound(), cacheDir)
        loadSound(SoundType.DROP, generateDropSound(), cacheDir)
        loadSound(SoundType.TUBE_COMPLETE, generateTubeCompleteSound(), cacheDir)
        loadSound(SoundType.LEVEL_WIN, generateLevelWinSound(), cacheDir)
        loadSound(SoundType.COMBO_LASER, generateComboLaserSound(), cacheDir)
        loadSound(SoundType.AIRHORN, generateAirhornSound(), cacheDir)
        loadSound(SoundType.ERROR_BOING, generateErrorBoingSound(), cacheDir)
        loadSound(SoundType.MAGIC_ADD, generateMagicAddSound(), cacheDir)
        loadSound(SoundType.REWIND, generateRewindSound(), cacheDir)
        loadSound(SoundType.CLICK, generateClickSound(), cacheDir)
        loadSound(SoundType.FIREWORKS, generateFireworksSound(), cacheDir)
    }

    private fun loadSound(type: SoundType, pcmData: ShortArray, cacheDir: File) {
        try {
            val wavFile = File(cacheDir, "${type.name.lowercase()}.wav")
            val wavBytes = createWavBytes(pcmData, 44100)
            FileOutputStream(wavFile).use { it.write(wavBytes) }
            val id = soundPool?.load(wavFile.absolutePath, 1) ?: 0
            if (id > 0) {
                soundIds[type] = id
            }
        } catch (e: Exception) {
            Log.e("CrazySoundEngine", "Failed to load ${type.name}", e)
        }
    }

    private fun play(type: SoundType, rate: Float = 1.0f, extraVolumeFactor: Float = 1.0f) {
        val id = soundIds[type] ?: return
        val vol = (volumeScale * extraVolumeFactor).coerceIn(0.0f, 1.0f)
        try {
            soundPool?.play(id, vol, vol, 1, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e("CrazySoundEngine", "Error playing sound", e)
        }
    }

    // --- Sound Triggers ---

    fun playBallSelect() {
        play(SoundType.POP, rate = 1.2f)
        vibrateLight()
    }

    fun playBallDrop(speedStep: Int = 0) {
        val pitch = 1.0f + (speedStep * 0.08f)
        play(SoundType.DROP, rate = pitch)
        vibrateMedium()
    }

    fun playCascade(step: Int) {
        val pitch = (1.0f + (step * 0.12f)).coerceAtMost(1.8f)
        play(SoundType.POP, rate = pitch, extraVolumeFactor = 1.1f)
        vibrateLight()
    }

    fun playTubeComplete() {
        play(SoundType.TUBE_COMPLETE, rate = 1.0f, extraVolumeFactor = 1.2f)
        if (isCrazyModeEnabled) {
            // In crazy mode, add a subtle fireworks crackle layer
            play(SoundType.FIREWORKS, rate = 1.1f, extraVolumeFactor = 0.8f)
        }
        vibrateSuccess()
    }

    fun playLevelWin() {
        play(SoundType.LEVEL_WIN, rate = 1.0f, extraVolumeFactor = 1.3f)
        if (isCrazyModeEnabled) {
            // Blast the crazy airhorn on victory!
            play(SoundType.AIRHORN, rate = 1.0f, extraVolumeFactor = 1.2f)
        }
        vibrateSuccess()
    }

    fun playComboStreak(streak: Int) {
        val pitch = (1.0f + (streak * 0.15f)).coerceAtMost(1.9f)
        play(SoundType.COMBO_LASER, rate = pitch, extraVolumeFactor = 1.15f)
        if (isCrazyModeEnabled && streak >= 3) {
            play(SoundType.AIRHORN, rate = 1.05f, extraVolumeFactor = 1.0f)
        }
        vibrateMedium()
    }

    fun playAirhorn() {
        play(SoundType.AIRHORN, rate = 1.0f, extraVolumeFactor = 1.4f)
        vibrateHeavy()
    }

    fun playError() {
        play(SoundType.ERROR_BOING, rate = 1.0f, extraVolumeFactor = 1.0f)
        vibrateError()
    }

    fun playAddTube() {
        play(SoundType.MAGIC_ADD, rate = 1.0f, extraVolumeFactor = 1.1f)
        vibrateMedium()
    }

    fun playUndo() {
        play(SoundType.REWIND, rate = 1.0f, extraVolumeFactor = 1.0f)
        vibrateLight()
    }

    fun playClick() {
        play(SoundType.CLICK, rate = 1.0f, extraVolumeFactor = 0.8f)
        vibrateLight()
    }

    fun playFireworks() {
        play(SoundType.FIREWORKS, rate = 1.0f, extraVolumeFactor = 1.2f)
        vibrateHeavy()
    }

    // --- Haptics ---

    fun vibrateLight() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, 80))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    fun vibrateMedium() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, 160))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    fun vibrateHeavy() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(80, 255))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    fun vibrateSuccess() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 50, 60, 50, 100)
                val amplitudes = intArrayOf(0, 120, 0, 180, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(180)
            }
        } catch (_: Exception) {}
    }

    fun vibrateError() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 60, 60)
                val amplitudes = intArrayOf(0, 220, 0, 180)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }

    // --- Synthesizer Waveform Generators ---

    private fun generatePopSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 120
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Rapid pitch sweep from 450 Hz up to 920 Hz
            val freq = 450.0 + 470.0 * progress
            val env = exp(-progress * 6.0)
            // Sine with second harmonic for rubber pop flavor
            val wave = sin(2.0 * PI * freq * t) + 0.4 * sin(4.0 * PI * freq * t)
            pcm[i] = softClip(wave * env * 28000.0)
        }
        return pcm
    }

    private fun generateDropSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 180
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Pitch drops from 360 Hz to 130 Hz with metallic tube overtone
            val freq = 360.0 - 230.0 * progress
            val env = exp(-progress * 5.0)
            val base = sin(2.0 * PI * freq * t)
            val glassHarmonic = 0.35 * sin(2.0 * PI * (freq * 3.2) * t) * exp(-progress * 12.0)
            val sample = (base + glassHarmonic) * env
            pcm[i] = softClip(sample * 30000.0)
        }
        return pcm
    }

    private fun generateTubeCompleteSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 650
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)

        // Ascending major arpeggio chords: C5 (523), E5 (659), G5 (784), C6 (1046)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteLengthSamples = numSamples / 5

        for (i in 0 until numSamples) {
            val noteIdx = min(i / noteLengthSamples, notes.size - 1)
            val noteFreq = notes[noteIdx]
            val localT = (i % noteLengthSamples).toDouble() / sampleRate
            val globalProgress = i.toDouble() / numSamples

            val vibrato = 1.0 + 0.015 * sin(2.0 * PI * 8.0 * (i.toDouble() / sampleRate))
            val freq = noteFreq * vibrato

            val noteEnv = exp(-(localT * 3.5))
            val globalEnv = 1.0 - globalProgress * 0.5
            val synth = sin(2.0 * PI * freq * localT) + 0.5 * sin(4.0 * PI * freq * localT) + 0.25 * sin(6.0 * PI * freq * localT)
            pcm[i] = softClip(synth * noteEnv * globalEnv * 26000.0)
        }
        return pcm
    }

    private fun generateLevelWinSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 1100
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)

        // Heroic fanfare chords: G4(392) -> C5(523) -> E5(659) -> G5(784) -> C6(1046)
        val chordTimes = doubleArrayOf(0.0, 0.18, 0.36, 0.54, 0.72)
        val chordFreqs = doubleArrayOf(392.0, 523.25, 659.25, 783.99, 1046.50)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sample = 0.0

            for (c in chordTimes.indices) {
                val startT = chordTimes[c]
                if (t >= startT) {
                    val dt = t - startT
                    val freq = chordFreqs[c]
                    val env = exp(-dt * 2.2)
                    // Bright synth brass
                    val brass = sin(2.0 * PI * freq * dt) +
                            0.6 * sin(4.0 * PI * freq * dt) +
                            0.3 * sin(6.0 * PI * freq * dt)
                    sample += brass * env * 0.5
                }
            }

            // Crowd shimmer noise burst on last part
            if (t > 0.7) {
                val noise = (Math.random() * 2.0 - 1.0) * 0.15 * exp(-(t - 0.7) * 2.5)
                sample += noise
            }

            pcm[i] = softClip(sample * 31000.0)
        }
        return pcm
    }

    private fun generateComboLaserSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 280
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Laser pitch drop from 2200 Hz down to 350 Hz with FM modulation
            val baseFreq = 2200.0 * (1.0 - progress * 0.85)
            val fmMod = sin(2.0 * PI * 40.0 * t) * 150.0
            val env = exp(-progress * 4.0)
            val wave = sin(2.0 * PI * (baseFreq + fmMod) * t)
            pcm[i] = softClip(wave * env * 30000.0)
        }
        return pcm
    }

    private fun generateAirhornSound(): ShortArray {
        // Iconic crazy DJ stadium air horn! (Triple loud blast)
        val sampleRate = 44100
        val durationMs = 700
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)

        // Airhorn frequencies: A#4 (466 Hz), D5 (587 Hz), F5 (698 Hz) with harsh overdrive
        val f1 = 466.16
        val f2 = 587.33
        val f3 = 698.46

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Rhythm: blast 1 (0.0 to 0.16s), gap (0.16 to 0.22s), blast 2 (0.22 to 0.38s), gap (0.38 to 0.44s), blast 3 (0.44 to 0.68s)
            val inBlast = (t in 0.0..0.16) || (t in 0.22..0.38) || (t in 0.44..0.68)
            if (!inBlast) {
                pcm[i] = 0
                continue
            }

            val blastPhase = when {
                t <= 0.16 -> t / 0.16
                t <= 0.38 -> (t - 0.22) / 0.16
                else -> (t - 0.44) / 0.24
            }
            val env = if (blastPhase < 0.1) blastPhase * 10.0 else exp(-(blastPhase - 0.1) * 1.5)

            // Heavy rich square-like distortion harmonics
            val horn = sin(2.0 * PI * f1 * t) +
                    0.8 * sin(2.0 * PI * f2 * t) +
                    0.7 * sin(2.0 * PI * f3 * t) +
                    0.4 * sin(2.0 * PI * (f1 * 2) * t)

            // Overdriven saturated sound
            val overdriven = (horn * 2.2).coerceIn(-1.0, 1.0)
            pcm[i] = (overdriven * env * 31500.0).toInt().toShort()
        }
        return pcm
    }

    private fun generateErrorBoingSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 320
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Comical cartoon boing with wobble
            val pitch = 220.0 + 90.0 * sin(2.0 * PI * 18.0 * t) - (progress * 80.0)
            val env = exp(-progress * 4.5)
            val wave = sin(2.0 * PI * pitch * t) + 0.3 * sin(4.0 * PI * pitch * t)
            pcm[i] = softClip(wave * env * 28000.0)
        }
        return pcm
    }

    private fun generateMagicAddSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 450
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Sci-fi magic ascension
            val freq = 400.0 + 1200.0 * (progress * progress)
            val shimmer = sin(2.0 * PI * 35.0 * t) * 60.0
            val env = (1.0 - progress) * (1.0 - exp(-progress * 15.0))
            val wave = sin(2.0 * PI * (freq + shimmer) * t)
            pcm[i] = softClip(wave * env * 27000.0)
        }
        return pcm
    }

    private fun generateRewindSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 250
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Downward rewind whoosh
            val freq = 1100.0 * exp(-progress * 3.5)
            val env = exp(-progress * 2.8)
            val wave = sin(2.0 * PI * freq * t)
            pcm[i] = softClip(wave * env * 26000.0)
        }
        return pcm
    }

    private fun generateClickSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 35
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val env = exp(-progress * 18.0)
            val noise = (Math.random() * 2.0 - 1.0) * 0.4
            val tone = sin(2.0 * PI * 1800.0 * t) * 0.6
            pcm[i] = softClip((tone + noise) * env * 24000.0)
        }
        return pcm
    }

    private fun generateFireworksSound(): ShortArray {
        val sampleRate = 44100
        val durationMs = 500
        val numSamples = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Low sub boom + crackle
            val subBass = sin(2.0 * PI * 75.0 * exp(-progress * 4.0) * t) * exp(-progress * 5.0)
            val crackle = if (Math.random() > 0.82) (Math.random() * 2.0 - 1.0) * exp(-progress * 3.0) else 0.0
            pcm[i] = softClip((subBass * 0.7 + crackle * 0.6) * 30000.0)
        }
        return pcm
    }

    private fun softClip(value: Double): Short {
        // Soft-clip saturation for loud punchy arcade audio without digital clicks
        val normalized = value / 32768.0
        val saturated = when {
            normalized > 1.0 -> 1.0 - (1.0 / (normalized + 1.0))
            normalized < -1.0 -> -1.0 + (1.0 / (-normalized + 1.0))
            else -> normalized - (normalized * normalized * normalized / 6.0)
        }
        return (saturated * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
    }

    private fun createWavBytes(pcmData: ShortArray, sampleRate: Int): ByteArray {
        val byteDataSize = pcmData.size * 2
        val totalSize = 36 + byteDataSize
        val buffer = ByteBuffer.allocate(44 + byteDataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size
        buffer.putShort(1) // AudioFormat PCM = 1
        buffer.putShort(1) // Channels = 1 (Mono)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2) // ByteRate
        buffer.putShort(2) // BlockAlign
        buffer.putShort(16) // BitsPerSample
        buffer.put("data".toByteArray())
        buffer.putInt(byteDataSize)
        for (sample in pcmData) {
            buffer.putShort(sample)
        }
        return buffer.array()
    }
}
