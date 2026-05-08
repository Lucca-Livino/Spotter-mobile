package dev.fslab.academia.ui.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object SessionSoundManager {

    private const val SAMPLE_RATE = 44100
    private const val MASTER_VOL  = 0.68f

    private val PARTIALS = listOf(
        Triple(1.0, 1.00f,  4.0f),
        Triple(2.0, 0.80f,  9.0f),
        Triple(3.0, 0.45f, 16.0f),
        Triple(4.0, 0.22f, 26.0f),
        Triple(5.0, 0.10f, 40.0f),
    )
    private val PARTIALS_SUM = PARTIALS.sumOf { it.second.toDouble() }.toFloat()

    // ── API pública ──────────────────────────────────────────────────────────────

    /**
     * Série / timer concluídos.
     * Arpejo rápido ascendente C5→E5→G5→C6 (padrão "achievement" de jogo).
     * Último acorde sustenta levemente — sensação de conclusão.
     */
    fun playSerieComplete() = playAsync {
        buildAndPlay(
            notes    = listOf(523f to 0, 659f to 65, 784f to 130, 1047f to 195),
            totalMs  = 780,
            tailMs   = 560
        )
    }

    /**
     * Descanso inicia.
     * Dois tons descendentes G5→E5 — curto, sinaliza pausa.
     */
    fun playRestStart() = playAsync {
        buildAndPlay(
            notes    = listOf(784f to 0, 659f to 120),
            totalMs  = 520,
            tailMs   = 380
        )
    }

    /**
     * Descanso termina.
     * Power-up ascendente G4→C5→E5→G5 — motivacional, estilo "go!".
     */
    fun playRestEnd() = playAsync {
        buildAndPlay(
            notes    = listOf(392f to 0, 523f to 70, 659f to 140, 784f to 210),
            totalMs  = 780,
            tailMs   = 540
        )
    }

    // ── Engine ────────────────────────────────────────────────────────────────

    /**
     * Mistura notas com offsets de tempo num único buffer.
     * [notes]: (frequência Hz, início em ms)
     * [totalMs]: duração total do buffer
     * [tailMs]: duração da cauda de cada nota (pode ultrapassar o gap até a próxima)
     */
    private fun buildAndPlay(
        notes   : List<Pair<Float, Int>>,
        totalMs : Int,
        tailMs  : Int
    ) {
        val numSamples   = SAMPLE_RATE * totalMs / 1000
        val tailSamples  = SAMPLE_RATE * tailMs  / 1000
        val attackSamples = maxOf((SAMPLE_RATE * 0.0012).toInt(), 1) // ~1.2ms — punch imediato
        val mix = FloatArray(numSamples)

        for ((freq, startMs) in notes) {
            val start = SAMPLE_RATE * startMs / 1000

            for (i in 0 until tailSamples) {
                val idx = start + i
                if (idx >= numSamples) break

                val t      = i.toDouble() / SAMPLE_RATE
                val attack = if (i < attackSamples) i.toFloat() / attackSamples else 1f

                var sample = 0.0
                for ((ratio, amp, decay) in PARTIALS) {
                    sample += sin(2.0 * PI * freq * ratio * t) * amp * exp(-decay * t)
                }

                mix[idx] += ((sample / PARTIALS_SUM) * attack * MASTER_VOL).toFloat()
            }
        }

        // Soft limiter — evita clipping sem perder punch
        val peak = mix.maxOfOrNull { kotlin.math.abs(it) }?.takeIf { it > 1f } ?: 1f
        val shortBuf = ShortArray(mix.size) { i ->
            ((mix[i] / peak) * Short.MAX_VALUE).toInt().toShort()
        }

        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(shortBuf.size * 2, minBuf))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(shortBuf, 0, shortBuf.size)
            track.play()
            Thread.sleep(totalMs.toLong() + 60)
        } finally {
            track.stop()
            track.release()
        }
    }

    private fun playAsync(block: () -> Unit) {
        Thread(block).apply { isDaemon = true }.start()
    }
}
