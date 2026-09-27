package ru.finnipet.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.PetSprite
import ru.finnipet.app.ui.components.StickerText
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.CreamDeep
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper

private data class IntroPage(val pose: PetPose, val title: String, val body: String)

private val PAGES = listOf(
    IntroPage(PetPose.WAVE, "Привет! Я Финни!", "Я маленький лисёнок, и мне очень нужен друг. Давай вместе научимся обращаться с деньгами!"),
    IntroPage(PetPose.TEACHER, "Учись и зарабатывай", "Отвечай на вопросы и играй в мини-игры — за это ты получишь монетки."),
    IntroPage(PetPose.PIGGY, "Трать, копи и делись", "Покупай мне еду и игрушки, копи на мечту в копилке и помогай другим."),
    IntroPage(PetPose.LOVE, "Как меня назовёшь?", ""),
)

@Composable
fun OnboardingScreen(onComplete: (String) -> Unit) {
    val pager = rememberPagerState { PAGES.size }
    val scope = rememberCoroutineScope()
    val feedback = rememberFeedback()
    val fx = LocalFx.current
    var name by remember { mutableStateOf("Финни") }
    var finishing by remember { mutableStateOf(false) }
    val last = pager.currentPage == PAGES.lastIndex

    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_onboarding), null,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
        )
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!last) {
                    Text(
                        "Пропустить",
                        style = MaterialTheme.typography.labelLarge,
                        color = Ink,
                        modifier = Modifier
                            .background(Paper.copy(alpha = 0.8f), RoundedCornerShape(50))
                            .clickable(remember { MutableInteractionSource() }, indication = null) {
                                feedback.tap()
                                scope.launch { pager.animateScrollToPage(PAGES.lastIndex) }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
            HorizontalPager(pager, Modifier.weight(1f)) { index ->
                val page = PAGES[index]
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    PetSprite(page.pose, height = 250.dp, contentDescription = "Финни")
                    Spacer(Modifier.height(12.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(28.dp))
                            .background(Paper, RoundedCornerShape(28.dp))
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        StickerText(page.title, MaterialTheme.typography.headlineMedium, color = FoxOrange, outline = Paper)
                        if (page.body.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(page.body, style = MaterialTheme.typography.bodyLarge, color = InkSoft, textAlign = TextAlign.Center)
                        }
                        if (index == 2) {
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                ConceptTile(R.drawable.ic_bag, "Трать", Modifier.weight(1f))
                                ConceptTile(R.drawable.ic_piggy, "Копи", Modifier.weight(1f))
                                ConceptTile(R.drawable.ic_gift, "Делись", Modifier.weight(1f))
                            }
                        }
                        if (index == PAGES.lastIndex) {
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(16) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, color = Ink),
                                shape = RoundedCornerShape(18.dp),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FoxOrange,
                                    unfocusedBorderColor = Line,
                                    focusedContainerColor = Paper,
                                    unfocusedContainerColor = Paper,
                                    cursorColor = FoxOrange,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            PageDots(PAGES.size, pager.currentPage, Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            ChunkyButton(
                text = if (last) "Поехали!" else "Дальше",
                color = if (last) ChunkyColor.GREEN else ChunkyColor.ORANGE,
                enabled = !last || name.isNotBlank(),
                sfx = if (last) null else Sfx.TAP,
                onClick = {
                    if (!last) {
                        scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    } else if (!finishing) {
                        finishing = true
                        feedback.success(Sfx.FANFARE)
                        fx.confetti()
                        scope.launch {
                            delay(900)
                            onComplete(name)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ConceptTile(@DrawableRes icon: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(CreamDeep, RoundedCornerShape(18.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(icon), null, Modifier.size(48.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = Ink)
    }
}

@Composable
private fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val width by animateDpAsState(if (i == current) 26.dp else 10.dp, label = "dot")
            val color by animateColorAsState(if (i == current) FoxOrange else Paper, label = "dotColor")
            Box(
                Modifier
                    .size(width = width, height = 10.dp)
                    .shadow(2.dp, RoundedCornerShape(50))
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}
