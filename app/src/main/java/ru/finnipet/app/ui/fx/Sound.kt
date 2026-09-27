package ru.finnipet.app.ui.fx

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import ru.finnipet.app.R

enum class Sfx(@RawRes val res: Int) {
    TAP(R.raw.sfx_tap),
    POP(R.raw.sfx_pop),
    COIN(R.raw.sfx_coin),
    COINS(R.raw.sfx_coins),
    CORRECT(R.raw.sfx_correct),
    WRONG(R.raw.sfx_wrong),
    PURCHASE(R.raw.sfx_purchase),
    EAT(R.raw.sfx_eat),
    LOVE(R.raw.sfx_love),
    WHOOSH(R.raw.sfx_whoosh),
    FANFARE(R.raw.sfx_fanfare),
    CHEST(R.raw.sfx_chest),
    LEVEL_UP(R.raw.sfx_levelup),
    TICK(R.raw.sfx_tick),
}

class SoundBoard(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val ids: Map<Sfx, Int> = Sfx.entries.associateWith { pool.load(context, it.res, 1) }

    var enabled: Boolean = true

    fun play(sfx: Sfx, volume: Float = 1f, rate: Float = 1f) {
        if (!enabled) return
        pool.play(ids.getValue(sfx), volume, volume, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    fun release() = pool.release()
}

val LocalSound = staticCompositionLocalOf<SoundBoard?> { null }

/** Sound plus a matching haptic, so every interaction answers back the same way. */
class Feedback(private val sound: SoundBoard?, private val haptic: HapticFeedback) {
    fun tap(sfx: Sfx = Sfx.TAP) {
        sound?.play(sfx)
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    fun success(sfx: Sfx = Sfx.CORRECT) {
        sound?.play(sfx)
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    fun error(sfx: Sfx = Sfx.WRONG) {
        sound?.play(sfx)
        haptic.performHapticFeedback(HapticFeedbackType.Reject)
    }

    fun play(sfx: Sfx, volume: Float = 1f, rate: Float = 1f) = sound?.play(sfx, volume, rate)

    fun buzz() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberFeedback(): Feedback {
    val sound = LocalSound.current
    val haptic = LocalHapticFeedback.current
    return remember(sound, haptic) { Feedback(sound, haptic) }
}
