package ru.finnipet.app.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ru.finnipet.app.R
import ru.finnipet.app.ui.theme.BerryPink
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.GrapePurple
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.SkyBlue
import ru.finnipet.app.ui.theme.SunGold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class Kind { COIN, IMAGE, CONFETTI, HEART, SPARKLE }

private class Particle(
    val kind: Kind,
    var x: Float,
    var y: Float,
    val duration: Float,
    val delay: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var rotation: Float = 0f,
    val spin: Float = 0f,
    val size: Float = 0f,
    val color: Color = Color.White,
    val phase: Float = 0f,
    val start: Offset = Offset.Zero,
    val control: Offset = Offset.Zero,
    val end: Offset = Offset.Zero,
    val image: ImageBitmap? = null,
    val onArrive: (() -> Unit)? = null,
) {
    var age = 0f
    val progress get() = ((age - delay) / duration).coerceIn(0f, 1f)
    val started get() = age >= delay
    val done get() = age >= delay + duration
}

private val CONFETTI_COLORS = listOf(FoxOrange, SunGold, LeafGreen, SkyBlue, BerryPink, GrapePurple)

/**
 * Tiny particle engine for rewards: coins flying to the counter, confetti, hearts, sparkles and items flying to
 * Finni. Everything is drawn in one full-screen canvas in root coordinates.
 */
class FxController {
    private val particles = ArrayList<Particle>()
    private val random = Random(42)
    internal var frame by mutableLongStateOf(0L)
    internal var running by mutableStateOf(false)
    internal var density = 3f
    internal var screen = Size.Zero

    /** Centre of the coin counter currently on screen, in root coordinates. */
    var coinTarget: Offset = Offset.Unspecified

    private val _coinArrived = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
    val coinArrived: SharedFlow<Unit> = _coinArrived

    private fun add(p: Particle) {
        particles += p
        running = true
    }

    private fun target(from: Offset): Offset =
        if (coinTarget.isSpecified()) coinTarget else Offset(from.x, from.y - 400f * density / 3f)

    fun coinsToCounter(from: Offset, count: Int = 6) {
        val to = target(from)
        repeat(count) { i ->
            val spread = Offset((random.nextFloat() - 0.5f) * 90f * density / 3f, (random.nextFloat() - 0.5f) * 60f * density / 3f)
            val start = from + spread
            val control = Offset((start.x + to.x) / 2f + (random.nextFloat() - 0.5f) * 160f, minOf(start.y, to.y) - 120f * density / 3f)
            add(Particle(Kind.COIN, start.x, start.y, duration = 0.62f, delay = i * 0.06f, start = start, control = control,
                end = to, size = 26f * density, spin = (random.nextFloat() - 0.5f) * 720f,
                onArrive = { _coinArrived.tryEmit(Unit) }))
        }
    }

    fun coinsFromCounter(to: Offset, count: Int = 4) {
        val from = target(to)
        repeat(count) { i ->
            val control = Offset((from.x + to.x) / 2f + (random.nextFloat() - 0.5f) * 120f, minOf(from.y, to.y) - 80f * density / 3f)
            add(Particle(Kind.COIN, from.x, from.y, duration = 0.5f, delay = i * 0.05f, start = from, control = control,
                end = to + Offset((random.nextFloat() - 0.5f) * 40f, 0f), size = 22f * density, spin = 360f))
        }
    }

    fun flyImage(image: ImageBitmap, from: Offset, to: Offset, sizePx: Float, onArrive: () -> Unit = {}) {
        val control = Offset((from.x + to.x) / 2f, minOf(from.y, to.y) - 160f * density / 3f)
        add(Particle(Kind.IMAGE, from.x, from.y, duration = 0.55f, start = from, control = control, end = to,
            size = sizePx, image = image, spin = 25f, onArrive = onArrive))
    }

    fun confetti(origin: Offset? = null, count: Int = 110) {
        repeat(count) {
            val fromTop = origin == null
            val x = if (fromTop) random.nextFloat() * screen.width else origin.x
            val y = if (fromTop) -20f - random.nextFloat() * screen.height * 0.25f else origin.y
            val angle = if (fromTop) PI.toFloat() / 2f else (-PI / 2 + (random.nextFloat() - 0.5f) * 2.2f).toFloat()
            val speed = (if (fromTop) 120f + random.nextFloat() * 160f else 600f + random.nextFloat() * 700f) * density / 3f
            add(Particle(Kind.CONFETTI, x, y, duration = 2.4f + random.nextFloat() * 0.8f,
                vx = cos(angle) * speed + (random.nextFloat() - 0.5f) * 80f, vy = sin(angle) * speed,
                rotation = random.nextFloat() * 360f, spin = (random.nextFloat() - 0.5f) * 600f,
                size = (7f + random.nextFloat() * 6f) * density / 3f * 1.6f,
                color = CONFETTI_COLORS[random.nextInt(CONFETTI_COLORS.size)], phase = random.nextFloat() * 6f))
        }
    }

    fun hearts(from: Offset, count: Int = 6) {
        repeat(count) { i ->
            add(Particle(Kind.HEART, from.x + (random.nextFloat() - 0.5f) * 80f * density / 3f, from.y,
                duration = 1.1f + random.nextFloat() * 0.4f, delay = i * 0.08f,
                vy = -(110f + random.nextFloat() * 70f) * density / 3f, size = (18f + random.nextFloat() * 10f) * density,
                phase = random.nextFloat() * 6f))
        }
    }

    fun sparkles(at: Offset, count: Int = 10, radius: Float = 60f) {
        repeat(count) {
            val angle = random.nextFloat() * 2f * PI.toFloat()
            val dist = (0.3f + random.nextFloat() * 0.7f) * radius * density / 3f
            add(Particle(Kind.SPARKLE, at.x + cos(angle) * dist, at.y + sin(angle) * dist,
                duration = 0.5f + random.nextFloat() * 0.3f, delay = random.nextFloat() * 0.15f,
                size = (10f + random.nextFloat() * 10f) * density, rotation = random.nextFloat() * 90f))
        }
    }

    internal fun step(dt: Float) {
        val gravity = 900f * density / 3f
        // Arrival callbacks may spawn new particles, so they run after the list has been walked.
        val arrivals = ArrayList<() -> Unit>()
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.age += dt
            if (!p.started) continue
            when (p.kind) {
                Kind.COIN, Kind.IMAGE -> {
                    val t = easeInOut(p.progress)
                    val pos = bezier(p.start, p.control, p.end, t)
                    p.x = pos.x
                    p.y = pos.y
                    p.rotation += p.spin * dt
                }
                Kind.CONFETTI -> {
                    p.vy += gravity * dt * (if (p.vy > 0) 0.35f else 1f)
                    p.vx *= 0.985f
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotation += p.spin * dt
                }
                Kind.HEART -> {
                    p.y += p.vy * dt
                    p.x += sin(p.age * 5f + p.phase) * 40f * dt * density / 3f
                }
                Kind.SPARKLE -> p.rotation += 120f * dt
            }
            if (p.done) {
                p.onArrive?.let(arrivals::add)
                iterator.remove()
            }
        }
        arrivals.forEach { it() }
        running = particles.isNotEmpty()
    }

    internal fun draw(scope: DrawScope, coin: ImageBitmap, heart: ImageBitmap, star: ImageBitmap) = with(scope) {
        for (p in particles) {
            if (!p.started) continue
            val t = p.progress
            when (p.kind) {
                Kind.COIN -> {
                    val pop = if (t < 0.2f) 0.6f + t * 2.5f else 1.1f - (t - 0.2f) * 0.35f
                    image(coin, p.x, p.y, p.size * pop, p.rotation * 0.15f, 1f)
                }
                Kind.IMAGE -> {
                    val scale = 1f - t * 0.45f
                    image(p.image!!, p.x, p.y, p.size * scale, p.rotation * t, 1f)
                }
                Kind.CONFETTI -> {
                    val alpha = if (t > 0.8f) (1f - t) / 0.2f else 1f
                    val flutter = abs(cos(p.age * 7f + p.phase))
                    rotate(p.rotation, Offset(p.x, p.y)) {
                        drawRect(p.color.copy(alpha = alpha), Offset(p.x - p.size / 2f, p.y - p.size * flutter / 3f),
                            Size(p.size, p.size * 0.66f * flutter + 1f))
                    }
                }
                Kind.HEART -> {
                    val grow = if (t < 0.25f) t / 0.25f else 1f
                    val alpha = if (t > 0.6f) (1f - t) / 0.4f else 1f
                    image(heart, p.x, p.y, p.size * (0.5f + 0.5f * grow), sin(p.age * 4f + p.phase) * 12f, alpha)
                }
                Kind.SPARKLE -> {
                    val scale = sin(t * PI.toFloat())
                    image(star, p.x, p.y, p.size * scale, p.rotation, 1f - t * 0.3f)
                }
            }
        }
    }

    private fun DrawScope.image(img: ImageBitmap, cx: Float, cy: Float, size: Float, degrees: Float, alpha: Float) {
        if (size <= 0.5f) return
        val w = size
        val h = size * img.height / img.width
        withTransform({ rotate(degrees, Offset(cx, cy)) }) {
            drawImage(img, IntOffset.Zero, IntSize(img.width, img.height),
                IntOffset((cx - w / 2f).toInt(), (cy - h / 2f).toInt()), IntSize(w.toInt(), h.toInt()), alpha = alpha)
        }
    }
}

private fun Offset.isSpecified() = this != Offset.Unspecified && !x.isNaN() && !y.isNaN()

private fun easeInOut(t: Float): Float = if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).let { it * it } / 2f

private fun bezier(a: Offset, c: Offset, b: Offset, t: Float): Offset {
    val u = 1f - t
    return Offset(u * u * a.x + 2f * u * t * c.x + t * t * b.x, u * u * a.y + 2f * u * t * c.y + t * t * b.y)
}

val LocalFx = staticCompositionLocalOf { FxController() }

@Composable
fun FxLayer(controller: FxController, modifier: Modifier = Modifier) {
    val coin = ImageBitmap.imageResource(R.drawable.ic_coin)
    val heart = ImageBitmap.imageResource(R.drawable.ic_heart)
    val star = ImageBitmap.imageResource(R.drawable.ic_star)
    controller.density = LocalDensity.current.density

    LaunchedEffect(controller.running) {
        if (!controller.running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (controller.running) {
            withFrameNanos { now ->
                controller.step(((now - last) / 1_000_000_000f).coerceAtMost(0.05f))
                last = now
                controller.frame = now
            }
        }
    }

    Canvas(modifier.fillMaxSize()) {
        controller.screen = size
        controller.frame
        controller.draw(this, coin, heart, star)
    }
}
