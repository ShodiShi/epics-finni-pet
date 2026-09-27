package ru.finnipet.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.finnipet.app.BuildConfig
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.PetPose
import ru.finnipet.app.ui.GameViewModel
import ru.finnipet.app.ui.components.ChunkyButton
import ru.finnipet.app.ui.components.ChunkyColor
import ru.finnipet.app.ui.components.PetSprite
import ru.finnipet.app.ui.components.RoundIconButton
import ru.finnipet.app.ui.fx.LocalFx
import ru.finnipet.app.ui.fx.Sfx
import ru.finnipet.app.ui.fx.rememberFeedback
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.FoxOrange
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.InkSoft
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper
import ru.finnipet.app.ui.theme.SkyBlueSoft
import ru.finnipet.app.ui.theme.TomatoRed

@Composable
fun SettingsScreen(state: GameState, viewModel: GameViewModel, onBack: () -> Unit) {
    val feedback = rememberFeedback()
    val fx = LocalFx.current
    val focus = LocalFocusManager.current
    var name by remember(state.petName) { mutableStateOf(state.petName) }
    var confirmReset by remember { mutableStateOf(false) }
    var versionTaps by rememberSaveable { mutableIntStateOf(0) }
    val demo = versionTaps >= 5
    val canSave = name.isNotBlank() && name.trim() != state.petName
    var petTop by remember { mutableStateOf(Offset.Zero) }
    var greeting by remember { mutableStateOf("") }
    var greetingSerial by remember { mutableIntStateOf(0) }
    var greetingShown by remember { mutableStateOf(false) }

    fun saveName() {
        focus.clearFocus()
        if (!canSave) return
        val newName = name.trim()
        viewModel.renamePet(newName)
        greeting = "Теперь меня зовут $newName!"
        greetingSerial++
        feedback.success(Sfx.LOVE)
        fx.hearts(petTop, 5)
    }

    LaunchedEffect(greetingSerial) {
        if (greetingSerial > 0) {
            greetingShown = true
            delay(2600)
            greetingShown = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(null, "Назад", onBack, size = 44.dp) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = InkSoft, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("Настройки", style = MaterialTheme.typography.headlineMedium, color = Ink)
        }
        Spacer(Modifier.height(16.dp))

        SettingsCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PetSprite(
                    if (greetingShown) PetPose.LOVE else PetPose.WAVE,
                    height = 96.dp,
                    modifier = Modifier.onGloballyPositioned {
                        val bounds = it.boundsInRoot()
                        petTop = Offset(bounds.center.x, bounds.top + bounds.height * 0.1f)
                    },
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Имя питомца", style = MaterialTheme.typography.labelLarge, color = InkSoft)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(16) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(color = Ink),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { saveName() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FoxOrange,
                            unfocusedBorderColor = Line,
                            focusedContainerColor = Paper,
                            unfocusedContainerColor = Paper,
                            cursorColor = FoxOrange,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            ChunkyButton(
                "Сохранить имя",
                onClick = ::saveName,
                enabled = canSave,
                color = ChunkyColor.GREEN,
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(greetingShown) {
                Text(
                    greeting,
                    style = MaterialTheme.typography.titleSmall,
                    color = LeafGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                )
            }
        }

        SettingsCard {
            ToggleRow(R.drawable.ic_music, "Музыка", state.musicOn) { viewModel.setMusic(it) }
            Spacer(Modifier.height(8.dp))
            ToggleRow(R.drawable.ic_sound, "Звуки", state.soundOn) { viewModel.setSound(it) }
        }

        SettingsCard {
            Text("Об игре", style = MaterialTheme.typography.titleLarge, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "«Питомец Финни» — игра о финансовой грамотности для детей 7–11 лет. " +
                    "Зарабатывай, трать с умом, копи на мечту и помогай другим вместе с Финни. " +
                    "Хакатон Департамента финансов города Москвы.",
                style = MaterialTheme.typography.bodyMedium, color = InkSoft,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Версия ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = InkSoft,
                modifier = Modifier.clickable(remember { MutableInteractionSource() }, indication = null) {
                    versionTaps++
                    if (versionTaps == 5) feedback.success(Sfx.LEVEL_UP)
                },
            )
        }

        if (demo) {
            SettingsCard(background = SkyBlueSoft) {
                Text("Режим презентации", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Быстро показать, как игра меняется со временем.", style = MaterialTheme.typography.bodySmall, color = InkSoft)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChunkyButton("Проголодаться", viewModel::demoHungry, color = ChunkyColor.ORANGE, height = 44.dp, modifier = Modifier.weight(1f))
                    ChunkyButton("Заскучать", viewModel::demoBored, color = ChunkyColor.BLUE, height = 44.dp, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChunkyButton("+100 монет", viewModel::demoCoins, color = ChunkyColor.GOLD, height = 44.dp, modifier = Modifier.weight(1f))
                    ChunkyButton("Новый день", viewModel::demoNextDay, color = ChunkyColor.GREEN, height = 44.dp, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        ChunkyButton(
            "Сбросить прогресс",
            onClick = { confirmReset = true },
            color = ChunkyColor.WHITE,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = Paper,
            icon = { PetSprite(PetPose.SAD, height = 90.dp, breathing = false) },
            title = { Text("Начать всё сначала?", style = MaterialTheme.typography.headlineSmall, color = Ink) },
            text = {
                Text(
                    "Монеты, копилка, награды и серия дней исчезнут навсегда.",
                    style = MaterialTheme.typography.bodyLarge, color = InkSoft,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    viewModel.resetProgress()
                }) { Text("Сбросить", style = MaterialTheme.typography.labelLarge, color = TomatoRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("Отмена", style = MaterialTheme.typography.labelLarge, color = Ink)
                }
            },
        )
    }
}

@Composable
private fun SettingsCard(background: Color = Paper, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .background(background, RoundedCornerShape(24.dp))
            .border(2.dp, Line, RoundedCornerShape(24.dp))
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun ToggleRow(icon: Int, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(icon), null, Modifier.size(34.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = LeafGreen,
                checkedThumbColor = Paper,
                uncheckedTrackColor = Line,
                uncheckedThumbColor = Paper,
                uncheckedBorderColor = Line,
            ),
        )
    }
}
