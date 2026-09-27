package ru.finnipet.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.domain.Question
import ru.finnipet.app.domain.todaysQuestions
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.ChunkySurface
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
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.LeafGreenLip
import ru.finnipet.app.ui.theme.LeafGreenSoft
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.LineStrong
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SkyBlue
import ru.finnipet.app.ui.theme.TomatoRed
import ru.finnipet.app.ui.theme.TomatoRedLip
import ru.finnipet.app.ui.theme.TomatoRedSoft

private val LETTERS = listOf("А", "Б", "В", "Г")

@Composable
fun QuizScreen(state: GameState, onAnswer: (String, Int) -> Unit, onClose: () -> Unit) {
    val questions = remember(state.currentDayEpoch) { todaysQuestions(state) }
    val firstOpen = questions.indexOfFirst { it.id !in state.currentDayAnsweredIds }
    var index by remember(state.currentDayEpoch) { mutableIntStateOf(if (firstOpen < 0) questions.size else firstOpen) }
    var chosen by remember(index) { mutableStateOf<Int?>(null) }
    val answered = questions.count { it.id in state.currentDayAnsweredIds }

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
            SegmentedProgress(questions.size.coerceAtLeast(1), answered, SkyBlue, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            CoinCounter(state.coins)
        }

        if (index >= questions.size) {
            QuizResults(state, questions.size, onClose)
            return@Column
        }

        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(260)) { -it / 2 } + fadeOut(tween(200)))
                },
                label = "question",
            ) { shownIndex ->
                QuestionPage(
                    question = questions[shownIndex],
                    chosen = if (shownIndex == index) chosen else null,
                    onChoose = { option ->
                        if (chosen == null) {
                            chosen = option
                            onAnswer(questions[shownIndex].id, option)
                        }
                    },
                )
            }
            val current = questions[index]
            androidx.compose.animation.AnimatedVisibility(
                visible = chosen != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(260)) { it } + fadeIn(),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(),
            ) {
                FeedbackPanel(
                    correct = chosen == current.correctIndex,
                    question = current,
                    last = index == questions.lastIndex,
                    onNext = { index++ },
                )
            }
        }
    }
}

@Composable
private fun QuestionPage(question: Question, chosen: Int?, onChoose: (Int) -> Unit) {
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    val pose = when {
        chosen == null -> PetPose.THINK
        chosen == question.correctIndex -> PetPose.HAPPY
        else -> PetPose.OOPS
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 230.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            PetSprite(pose, height = 130.dp)
            Spacer(Modifier.width(4.dp))
            SpeechBubble(question.text, side = TailSide.LEFT, maxWidth = 260.dp)
        }
        Spacer(Modifier.height(8.dp))
        question.options.forEachIndexed { i, option ->
            OptionCard(
                letter = LETTERS[i],
                text = option,
                state = when {
                    chosen == null -> OptionState.IDLE
                    i == question.correctIndex -> OptionState.CORRECT
                    i == chosen -> OptionState.WRONG
                    else -> OptionState.DIMMED
                },
                onClick = { center ->
                    onChoose(i)
                    if (i == question.correctIndex) {
                        feedback.success(Sfx.CORRECT)
                        fx.sparkles(center, 12, 110f)
                        fx.coinsToCounter(center, 5)
                    } else {
                        feedback.error(Sfx.WRONG)
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

private enum class OptionState { IDLE, CORRECT, WRONG, DIMMED }

@Composable
private fun OptionCard(letter: String, text: String, state: OptionState, onClick: (Offset) -> Unit) {
    var center by remember { mutableStateOf(Offset.Zero) }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == OptionState.WRONG) {
            shake.snapTo(0f)
            shake.animateTo(0f, keyframes {
                durationMillis = 420
                -14f at 50
                14f at 110
                -10f at 170
                10f at 230
                -5f at 300
                5f at 360
            })
        }
    }
    val (face, lip, border, badge) = when (state) {
        OptionState.CORRECT -> listOf(LeafGreenSoft, LeafGreenLip, LeafGreen, LeafGreen)
        OptionState.WRONG -> listOf(TomatoRedSoft, TomatoRedLip, TomatoRed, TomatoRed)
        else -> listOf(Paper, LineStrong, Line, FoxOrange)
    }
    ChunkySurface(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(shake.value.dp.roundToPx(), 0) }
            .alpha(if (state == OptionState.DIMMED) 0.55f else 1f)
            .onGloballyPositioned { center = it.boundsInRoot().center },
        face = face,
        lip = lip,
        border = border,
        onClick = if (state == OptionState.IDLE) ({ onClick(center) }) else null,
        sfx = null,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(badge, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(letter, style = MaterialTheme.typography.titleMedium, color = Paper)
            }
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

@Composable
private fun FeedbackPanel(correct: Boolean, question: Question, last: Boolean, onNext: () -> Unit) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (correct) LeafGreenSoft else TomatoRedSoft, shape)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(if (correct) R.drawable.ic_star else R.drawable.ic_book), null, Modifier.size(40.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                if (correct) "Верно! +${question.reward}" else "Почти! Правильно: «${question.options[question.correctIndex]}»",
                style = MaterialTheme.typography.titleLarge,
                color = if (correct) LeafGreenLip else TomatoRedLip,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(question.explanation, style = MaterialTheme.typography.bodyLarge, color = Ink)
        Spacer(Modifier.height(14.dp))
        ChunkyButton(
            text = if (last) "Посмотреть итог" else if (correct) "Дальше" else "Понятно, дальше",
            onClick = onNext,
            color = if (correct) ChunkyColor.GREEN else ChunkyColor.ORANGE,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuizResults(state: GameState, total: Int, onClose: () -> Unit) {
    val fx = LocalFx.current
    val feedback = rememberFeedback()
    val streakToday = state.lastStreakCreditEpoch == state.currentDayEpoch
    LaunchedEffect(Unit) {
        feedback.success(Sfx.FANFARE)
        fx.confetti()
    }
    val great = state.correctToday * 5 >= total * 4
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            SunburstRays(Modifier.fillMaxSize())
            PetSprite(if (great) PetPose.CHEER else PetPose.HAPPY, height = 210.dp)
        }
        StickerText(
            if (great) "Отличная работа!" else "Хорошая попытка!",
            MaterialTheme.typography.headlineLarge,
            color = FoxOrange,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (great) "Ты настоящий знаток денег!" else "Каждый ответ делает тебя умнее. Завтра будут новые вопросы!",
            style = MaterialTheme.typography.bodyLarge, color = InkSoft, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResultTile(R.drawable.ic_star, "${state.correctToday} из $total", "верно", Modifier.weight(1f))
            ResultTile(R.drawable.ic_coin, "+${state.coinsEarnedToday}", "монет", Modifier.weight(1f))
            if (streakToday) ResultTile(R.drawable.ic_flame, "${state.streakDays}", plural(state.streakDays, "день", "дня", "дней"), Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        ChunkyButton("Готово", onClose, color = ChunkyColor.GREEN, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun ResultTile(icon: Int, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Paper, RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(icon), null, Modifier.size(36.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(label, style = MaterialTheme.typography.labelMedium, color = InkSoft)
    }
}
