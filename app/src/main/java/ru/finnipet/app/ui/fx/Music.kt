package ru.finnipet.app.ui.fx

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Loops a 16-bit mono WAV gaplessly. A static AudioTrack with loop points avoids the pause MediaPlayer leaves
 * at the end of every loop.
 */
class LoopPlayer(context: Context, @RawRes res: Int, volume: Float) {
    private val track: AudioTrack? = runCatching {
        val (pcm, rate) = context.resources.openRawResource(res).use(::readWav)
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(rate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
            .apply {
                write(pcm, 0, pcm.size)
                setLoopPoints(0, pcm.size, -1)
                setVolume(volume)
            }
    }.getOrNull()

    fun play() {
        track?.takeIf { it.playState != AudioTrack.PLAYSTATE_PLAYING }?.play()
    }

    fun pause() {
        track?.takeIf { it.playState == AudioTrack.PLAYSTATE_PLAYING }?.pause()
    }

    fun release() {
        track?.release()
    }
}

private fun readWav(input: InputStream): Pair<ShortArray, Int> {
    val bytes = ByteBuffer.wrap(input.readBytes()).order(ByteOrder.LITTLE_ENDIAN)
    var rate = 44100
    bytes.position(12)
    while (bytes.remaining() >= 8) {
        val id = ByteArray(4).also { bytes.get(it) }.toString(Charsets.US_ASCII)
        val size = bytes.int
        when (id) {
            "fmt " -> {
                val start = bytes.position()
                bytes.short // format
                bytes.short // channels
                rate = bytes.int
                bytes.position(start + size)
            }
            "data" -> {
                val samples = ShortArray(size / 2)
                bytes.asShortBuffer().get(samples)
                return samples to rate
            }
            else -> bytes.position(bytes.position() + size + (size and 1))
        }
    }
    error("no data chunk")
}

/** Plays the background loop while the app is in the foreground and music is switched on. */
@Composable
fun BackgroundMusic(enabled: Boolean, @RawRes res: Int, volume: Float = 0.3f) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = remember(res) { LoopPlayer(context.applicationContext, res, volume) }
    DisposableEffect(player) { onDispose { player.release() } }
    val lifecycleState by lifecycle.currentStateFlow.collectAsState()
    val foreground = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    LaunchedEffect(enabled, foreground) {
        if (enabled && foreground) player.play() else player.pause()
    }
}
