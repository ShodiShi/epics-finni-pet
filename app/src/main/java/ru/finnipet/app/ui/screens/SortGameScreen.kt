package ru.finnipet.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.Economy
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.SortCard
import ru.finnipet.app.domain.sortDoneToday
import ru.finnipet.app.domain.todaysSortCards
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.CoinCounter
import ru.finnipet.app.ui.components.PetSprite
import ru.finnipet.app.ui.components.RoundIconButton
import ru.finnipet.app.ui.components.SegmentedProgress
import ru.finnipet.app.ui.components.SpeechBubble
import ru.finnipet.app.ui.components.StickerText
import ru.finnipet.app.ui.components.SunburstRays
import ru.finnipet.app.ui.components.TailSide
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.BerryPink
import ru.finnipet.app.ui.theme.BerryPinkSoft
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.LeafGreenSoft
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SkyBlue
import ru.finnipet.app.ui.theme.SunGoldSoft
import kotlin.math.abs

@Composable
fun SortGameScreen(
    state: GameState,
    onAnswer: (Boolean) -> Unit,
    onFinish: (Int, Int) -> Unit,
    onClose: () -> Unit,
) {
    val deck = remember(state.currentDayEpoch) { todaysSortCards(state) }
    val alreadyPlayed = remember { sortDoneToday(state) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var hint by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    val scope = rememberCoroutineScope()
    val dragX = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val peekIn = remember { Animatable(0f) }
    var cardCenter by remember { mutableStateOf(Offset.Zero) }
    val finished = index >= deck.size

    LaunchedEffect(finished) {
        if (finished && !alreadyPlayed && deck.isNotEmpty()) onFinish(score, deck.size)
    }
    LaunchedEffect(index) {
        peekIn.snapTo(0f)
        peekIn.animateTo(1f, tween(260))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(null, "Закрыть", onClose, size = 42.dp) {
                Icon(Icons.Rounded.Close, null, tint = InkSoft, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            SegmentedProgress(deck.size.coerceAtLeast(1), index.coerceAtMost(deck.size), SkyBlue, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            CoinCounter(state.coins)
        }

        when {
            alreadyPlayed -> SortSummary(state.sortBestScore, deck.size, replayBlocked = true, onClose = onClose)
            finished -> SortSummary(score, deck.size, replayBlocked = false, onClose = onClose)
            else -> BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val width = with(LocalDensity.current) { maxWidth.toPx() }
                val card = deck[index]

                fun decide(guessNeed: Boolean) {
                    if (busy) return
                    busy = true
                    val correct = card.isNeed == guessNeed
                    onAnswer(correct)
                    scope.launch {
                        if (correct) {
                            score++
                            feedback.success(Sfx.COIN)
                            fx.coinsToCounter(cardCenter, Economy.SORT_REWARD + 1)
                            fx.sparkles(cardCenter, 10, 100f)
                            dragX.animateTo(if (guessNeed) -width * 1.4f else width * 1.4f, tween(260))
                        } else {
                            feedback.error(Sfx.WRONG)
                            hint = card.hint
                            dragX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            shake.animateTo(0f, keyframes {
                                durationMillis = 420
                                -16f at 60
                                16f at 120
                                -10f at 190
                                10f at 250
                                0f at 420
                            })
                            delay(1500)
                            dragX.animateTo(if (card.isNeed) -width * 1.4f else width * 1.4f, tween(320))
                            hint = null
                        }
                        index++
                        dragX.snapTo(0f)
                        busy = false
                    }
                }

                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PetSprite(if (hint != null) PetPose.OOPS else PetPose.THINK, height = 96.dp)
                        Spacer(Modifier.width(4.dp))
                        SpeechBubble(
                            hint ?: "Это нужно или хочется? Смахни карточку влево или вправо!",
                            side = TailSide.LEFT,
                            maxWidth = 250.dp,
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            // The gesture lives on this steady area rather than on the moving card, so positions and
                            // velocity are measured in stable coordinates (and the whole area can be swiped).
                            .pointerInput(index) {
                                // Small hands rarely drag far, so a short drag or a quick flick both count.
                                val flingSpeed = 400.dp.toPx()
                                val minFling = 24.dp.toPx()
                                val tracker = VelocityTracker()
                                // Tracked here synchronously: the card's Animatable only catches up on the next
                                // frame, which a quick flick can beat.
                                var dragged = 0f
                                detectDragGestures(
                                    onDragStart = {
                                        tracker.resetTracking()
                                        dragged = dragX.value
                                    },
                                    onDragEnd = {
                                        val speed = tracker.calculateVelocity().x
                                        val flung = abs(speed) > flingSpeed && speed * dragged > 0 && abs(dragged) > minFling
                                        if (!busy && (abs(dragged) > width * 0.18f || flung)) {
                                            decide(guessNeed = dragged < 0)
                                        } else if (!busy) {
                                            scope.launch { dragX.animateTo(0f, spring(dampingRatio = 0.6f)) }
                                        }
                                    },
                                    onDragCancel = { if (!busy) scope.launch { dragX.animateTo(0f) } },
                                ) { change, amount ->
                                    if (!busy) {
                                        change.consume()
                                        tracker.addPointerInputChange(change)
                                        dragged += amount.x
                                        scope.launch { dragX.snapTo(dragged) }
                                    }
                                }
                            }
                            .padding(horizontal = 36.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (index + 1 < deck.size) {
                            // the next card waits just behind and rises into place as the top one is dragged away
                            SortCardView(
                                deck[index + 1],
                                Modifier.graphicsLayer {
                                    val lift = (abs(dragX.value) / (width * 0.5f)).coerceIn(0f, 1f)
                                    val scale = 0.93f + 0.07f * lift
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = 16.dp.toPx() * (1f - lift)
                                    alpha = peekIn.value
                                },
                            )
                        }
                        val progress = (dragX.value / (width * 0.35f)).coerceIn(-1f, 1f)
                        SortCardView(
                            card,
                            Modifier
                                .onGloballyPositioned { cardCenter = it.boundsInRoot().center }
                                .graphicsLayer {
                                    translationX = dragX.value + shake.value.dp.toPx()
                                    rotationZ = dragX.value / 24f
                                },
                            needStamp = (-progress).coerceAtLeast(0f),
                            wantStamp = progress.coerceAtLeast(0f),
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // taps while a card is flying away are ignored by decide(), so the buttons keep their colour
                        ChunkyButton("← Нужно", { decide(true) }, color = ChunkyColor.GREEN, icon = R.drawable.sort_water,
                            modifier = Modifier.weight(1f), sfx = null)
                        ChunkyButton("Хочется →", { decide(false) }, color = ChunkyColor.PINK, icon = R.drawable.sort_candy,
                            modifier = Modifier.weight(1f), sfx = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun SortCardView(card: SortCard, modifier: Modifier = Modifier, needStamp: Float = 0f, wantStamp: Float = 0f) {
    Box(
        modifier
            .fillMaxWidth()
            .height(360.dp)
            .shadow(14.dp, RoundedCornerShape(32.dp))
            .background(Paper, RoundedCornerShape(32.dp))
            .border(3.dp, Line, RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(200.dp)
                    .background(SunGoldSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(card.art), card.name, Modifier.size(150.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(card.name, style = MaterialTheme.typography.headlineSmall, color = Ink, textAlign = TextAlign.Center)
        }
        Stamp("НУЖНО", LeafGreen, LeafGreenSoft, needStamp, Modifier.align(Alignment.TopStart).padding(18.dp), -14f)
        Stamp("ХОЧЕТСЯ", BerryPink, BerryPinkSoft, wantStamp, Modifier.align(Alignment.TopEnd).padding(18.dp), 14f)
    }
}

@Composable
private fun Stamp(
    text: String,
    color: Color,
    fill: Color,
    strength: Float,
    modifier: Modifier,
    tilt: Float,
) {
    if (strength <= 0.02f) return
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        modifier = modifier
            .graphicsLayer {
                alpha = strength
                rotationZ = tilt
                scaleX = 0.8f + 0.2f * strength
                scaleY = 0.8f + 0.2f * strength
            }
            .background(fill, RoundedCornerShape(12.dp))
            .border(3.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun SortSummary(score: Int, total: Int, replayBlocked: Boolean, onClose: () -> Unit) {
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    val perfect = !replayBlocked && total > 0 && score == total
    LaunchedEffect(Unit) {
        if (!replayBlocked) {
            feedback.success(Sfx.FANFARE)
            fx.confetti()
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            SunburstRays(Modifier.fillMaxSize())
            PetSprite(if (replayBlocked) PetPose.WAVE else PetPose.CHEER, height = 200.dp)
        }
        StickerText(
            when {
                replayBlocked -> "На сегодня игра пройдена!"
                perfect -> "Без единой ошибки!"
                else -> "Хорошо разложено!"
            },
            MaterialTheme.typography.headlineMedium,
            color = FoxOrange,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (replayBlocked) {
                "Новая колода карточек появится завтра. Лучший результат: $score из $total."
            } else {
                "Нужное — то, без чего не обойтись. Хочется — то, что просто приятно. Ты молодец!"
            },
            style = MaterialTheme.typography.bodyLarge, color = InkSoft, textAlign = TextAlign.Center,
        )
        if (!replayBlocked) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultTile(R.drawable.ic_star, "$score из $total", "верно", Modifier.weight(1f))
                ResultTile(R.drawable.ic_coin, "+${score * Economy.SORT_REWARD}", "монет", Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(22.dp))
        ChunkyButton("Готово", onClose, color = ChunkyColor.GREEN, modifier = Modifier.fillMaxWidth())
    }
}

