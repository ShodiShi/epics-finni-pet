package ru.finnipet.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.Economy
import ru.finnipet.app.domain.ItemCategory
import ru.finnipet.app.domain.PetNeed
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.chestClaimedToday
import ru.finnipet.app.domain.chestReady
import ru.finnipet.app.domain.levelForXp
import ru.finnipet.app.domain.levelProgress
import ru.finnipet.app.domain.petNeed
import ru.finnipet.app.domain.questsDone
import ru.finnipet.app.domain.quizDoneToday
import ru.finnipet.app.domain.restingPose
import ru.finnipet.app.domain.savingsGoalById
import ru.finnipet.app.ui.LocalBarInset
import ru.finnipet.app.ui.LocalSheets
import ru.finnipet.app.ui.components.Anchored
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.CoinCounter
import ru.finnipet.app.ui.components.GroundShadow
import ru.finnipet.app.ui.components.LevelChip
import ru.finnipet.app.ui.components.PetSprite
import ru.finnipet.app.ui.components.RoundIconButton
import ru.finnipet.app.ui.components.SpeechBubble
import ru.finnipet.app.ui.components.StatMeter
import ru.finnipet.app.ui.components.StreakChip
import ru.finnipet.app.ui.components.Tab
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.BerryPink
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SunGold
import kotlin.math.max

/** Where things stand in the room painting, as fractions of the painting (bottom-centre anchors). */
private val PET_SPOT = Offset(0.5f, 0.815f)
private const val PET_HEIGHT = 0.30f

private data class Decoration(val id: String, val spot: Offset, val width: Float)

private val DECORATIONS = listOf(
    Decoration("goal_bike", Offset(0.19f, 0.69f), 0.36f),
    Decoration("goal_house", Offset(0.83f, 0.70f), 0.30f),
    Decoration("goal_bed", Offset(0.82f, 0.86f), 0.30f),
    Decoration("goal_zoo", Offset(0.15f, 0.87f), 0.21f),
)

private val PET_LINES = listOf(
    "Хи-хи, щекотно!", "Я тебя люблю!", "Фыр-фыр! Это по-лисьи «спасибо»!", "Ещё! Ещё!", "Ты мой лучший друг!",
)

private val TIPS = listOf(
    "Копить — значит откладывать понемногу, но регулярно.",
    "Сначала — то, что нужно, потом — то, что хочется!",
    "Не хватает денег? Подожди и накопи!",
    "Деньги в копилке растут сами — это проценты!",
    "Делиться — это здорово! Загляни в копилку добрых дел.",
    "Перед покупкой спроси себя: мне это правда нужно?",
)

@Composable
fun HomeScreen(
    state: GameState,
    onUseItem: (String) -> Unit,
    onPet: () -> Unit,
    onClaimChest: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigate: (Tab) -> Unit,
) {
    val fx = LocalFx.current
    val sheets = LocalSheets.current
    val feedback = rememberFeedback()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val barInset = LocalBarInset.current

    var reaction by remember { mutableStateOf<PetPose?>(null) }
    var reactionSerial by remember { mutableIntStateOf(0) }
    var said by remember { mutableStateOf<String?>(null) }
    var tip by remember { mutableIntStateOf(0) }
    var petRect by remember { mutableStateOf(Rect.Zero) }
    val jump = remember { Animatable(0f) }

    fun react(pose: PetPose, line: String?, millis: Long = 1600) {
        reaction = pose
        said = line
        reactionSerial++
        val serial = reactionSerial
        scope.launch {
            delay(millis)
            if (reactionSerial == serial) reaction = null
            delay(2200)
            if (reactionSerial == serial) said = null
        }
    }

    fun hop() = scope.launch {
        jump.animateTo(-1f, tween(130))
        jump.animateTo(0f, spring(dampingRatio = 0.32f, stiffness = Spring.StiffnessMediumLow))
    }

    /** Hearts rise from just above the ears, so they never cover Finni's face. */
    fun aboveHead() = Offset(petRect.center.x, petRect.top + petRect.height * 0.06f)

    fun openInventory(category: ItemCategory) {
        sheets.show { current ->
            InventorySheet(
                state = current,
                category = category,
                onUse = { item, from, bitmap ->
                    sheets.hide()
                    onUseItem(item.id)
                    val mouth = Offset(petRect.center.x, petRect.top + petRect.height * 0.36f)
                    fx.flyImage(bitmap, from, mouth, with(density) { 72.dp.toPx() }) {
                        if (item.category == ItemCategory.NEED) {
                            feedback.success(Sfx.EAT)
                            react(PetPose.EATING, "Ням-ням! Вкусно — спасибо за ${item.name.lowercase()}!")
                        } else {
                            feedback.success(Sfx.LOVE)
                            react(PetPose.PLAY, "Ура! ${item.name} — это так весело!")
                            fx.hearts(aboveHead(), 5)
                        }
                        fx.sparkles(mouth, 10, 90f)
                        hop()
                    }
                },
                onShop = {
                    sheets.hide()
                    onNavigate(Tab.SHOP)
                },
            )
        }
    }

    fun openQuests() {
        sheets.show { current ->
            QuestsSheet(current, onClaim = { chestCenter ->
                onClaimChest()
                feedback.success(Sfx.CHEST)
                fx.sparkles(chestCenter, 16, 110f)
                fx.coinsToCounter(chestCenter, 10)
                scope.launch {
                    delay(700)
                    feedback.play(Sfx.COINS)
                }
            })
        }
    }

    val pose = reaction ?: restingPose(state)
    val bubble = said ?: contextualLine(state, tip)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val panelHeight = 150.dp
        val roomHeight = maxHeight - barInset - panelHeight + 32.dp
        val room = painterResource(R.drawable.bg_room)
        val widthPx = with(density) { maxWidth.toPx() }
        val roomPx = with(density) { roomHeight.toPx() }
        val scale = max(widthPx / room.intrinsicSize.width, roomPx / room.intrinsicSize.height)
        val drawnW = room.intrinsicSize.width * scale
        val drawnH = room.intrinsicSize.height * scale
        val left = (widthPx - drawnW) / 2f
        val top = roomPx - drawnH
        fun spot(p: Offset) = Offset(left + p.x * drawnW, top + p.y * drawnH)
        val petHeight: Dp = with(density) { (drawnH * PET_HEIGHT).toDp() }.coerceIn(170.dp, 280.dp)

        Box(
            Modifier
                .fillMaxWidth()
                .height(roomHeight)
                .clipToBounds()
        ) {
            Image(
                room, null,
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter,
            )
            DECORATIONS.filter { it.id in state.ownedDecorations }.forEach { deco ->
                val width = with(density) { (drawnW * deco.width).toDp() }
                Anchored({ spot(deco.spot) }) {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        GroundShadow(width * 0.9f, Modifier.offset(y = width * 0.06f))
                        Image(painterResource(savingsGoalById(deco.id).art), null, Modifier.width(width))
                    }
                }
            }
            Anchored({ spot(PET_SPOT) }) {
                val lift = jump.value
                Box(contentAlignment = Alignment.BottomCenter) {
                    GroundShadow(
                        petHeight * 0.62f,
                        Modifier
                            .offset(y = petHeight * 0.05f)
                            .graphicsLayer { scaleX = 1f + lift * 0.25f; scaleY = 1f + lift * 0.25f },
                    )
                    PetSprite(
                        pose,
                        height = petHeight,
                        contentDescription = state.petName,
                        modifier = Modifier
                            .graphicsLayer { translationY = lift * 34.dp.toPx() }
                            .onGloballyPositioned { petRect = it.boundsInRoot() }
                            .clickable(remember { MutableInteractionSource() }, indication = null) {
                                onPet()
                                feedback.success(Sfx.LOVE)
                                fx.hearts(aboveHead())
                                hop()
                                val line = if (state.petsToday >= Economy.MAX_PETS_PER_DAY) {
                                    "Хи-хи, хватит, я уже весь сияю!"
                                } else {
                                    PET_LINES[(state.petsToday + reactionSerial) % PET_LINES.size]
                                }
                                react(PetPose.LOVE, line, 1400)
                            },
                    )
                }
            }
            Anchored({ spot(PET_SPOT) - Offset(0f, with(density) { (petHeight + 14.dp).toPx() }) }) {
                SpeechBubble(bubble, onClick = {
                    feedback.tap(Sfx.POP)
                    said = null
                    tip++
                })
            }
            ChestButton(
                state = state,
                onClick = ::openQuests,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp)
                    .offset(y = (-24).dp),
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (into, span) = levelProgress(state.xp)
            LevelChip(levelForXp(state.xp), into.toFloat() / span)
            Spacer(Modifier.width(8.dp))
            StreakChip(state.streakDays)
            Spacer(Modifier.weight(1f))
            CoinCounter(state.coins)
            Spacer(Modifier.width(8.dp))
            RoundIconButton(null, "Настройки", onOpenSettings, size = 44.dp) {
                Icon(Icons.Rounded.Settings, null, tint = InkSoft, modifier = Modifier.size(26.dp))
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = barInset)
                .fillMaxWidth()
                .height(panelHeight)
                .shadow(14.dp, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Cream, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        ) {
            Row {
                StatMeter("Сытость", R.drawable.item_apple, state.hunger, LeafGreen, Modifier.weight(1f))
                Spacer(Modifier.width(14.dp))
                StatMeter("Радость", R.drawable.ic_heart, state.happiness, BerryPink, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Row {
                ChunkyButton(
                    "Покормить", onClick = { openInventory(ItemCategory.NEED) },
                    color = ChunkyColor.GREEN, icon = R.drawable.item_apple, modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                ChunkyButton(
                    "Поиграть", onClick = { openInventory(ItemCategory.WANT) },
                    color = ChunkyColor.BLUE, icon = R.drawable.item_ball, modifier = Modifier.weight(1f),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(9000)
            if (said == null) tip++
        }
    }
}

private fun contextualLine(state: GameState, tip: Int): String {
    val goal = savingsGoalById(state.savingsGoalId)
    return when {
        petNeed(state) == PetNeed.HUNGRY -> "Я проголодался… Покормишь меня?"
        petNeed(state) == PetNeed.BORED -> "Мне скучно… Давай поиграем!"
        chestReady(state) -> "Все задания готовы! Открой сундук!"
        tip == 0 -> "Привет! Я ${state.petName}. Я так рад тебя видеть!"
        !quizDoneToday(state) && tip % 3 == 1 -> "Ответим на вопросы дня? Там монетки!"
        state.savingsBalance * 5 >= goal.target * 4 && tip % 3 == 2 ->
            "Ещё чуть-чуть — и у нас будет «${goal.title}»!"
        else -> TIPS[(tip - 1).mod(TIPS.size)]
    }
}

@Composable
private fun ChestButton(state: GameState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val done = questsDone(state)
    val ready = chestReady(state)
    val feedback = rememberFeedback()
    val infinite = rememberInfiniteTransition(label = "chest")
    val wiggle by infinite.animateFloat(-8f, 8f, infiniteRepeatable(tween(170), RepeatMode.Reverse), label = "wiggle")
    val glow by infinite.animateFloat(0.4f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "glow")
    Column(
        modifier
            .semantics { contentDescription = "Задания дня: $done из 3" }
            .clickable(remember { MutableInteractionSource() }, indication = null) {
                feedback.tap(Sfx.POP)
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
            if (ready) {
                Box(
                    Modifier
                        .size(76.dp)
                        .graphicsLayer { alpha = glow }
                        .background(SunGold.copy(alpha = 0.55f), CircleShape)
                )
            }
            Box(
                Modifier
                    .size(64.dp)
                    .shadow(6.dp, CircleShape)
                    .background(Paper, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
            Canvas(Modifier.size(64.dp)) {
                val stroke = 5.dp.toPx()
                drawArc(Line, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke))
                drawArc(SunGold, -90f, 360f * done / 3f, false, style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke))
            }
            Image(
                painterResource(if (chestClaimedToday(state)) R.drawable.ic_chest_open else R.drawable.ic_chest),
                null,
                Modifier
                    .size(42.dp)
                    .graphicsLayer { rotationZ = if (ready) wiggle else 0f },
            )
        }
        Text(
            if (ready) "Открой!" else "$done/3",
            style = MaterialTheme.typography.labelMedium,
            color = Ink,
            modifier = Modifier
                .offset(y = (-4).dp)
                .background(Paper, RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
