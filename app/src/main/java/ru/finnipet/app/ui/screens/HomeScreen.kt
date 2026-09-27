package ru.finnipet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.ItemCategory
import ru.finnipet.app.domain.PetMood
import ru.finnipet.app.domain.SHOP_ITEMS
import ru.finnipet.app.domain.deriveMood
import ru.finnipet.app.ui.components.CoinChip
import ru.finnipet.app.ui.components.PetHeroImage
import ru.finnipet.app.ui.components.StatBar
import ru.finnipet.app.ui.theme.LeafGreen
import ru.finnipet.app.ui.theme.SkyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: GameState,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mood = deriveMood(state.hunger, state.happiness)

    val hasFood = state.inventory.any { (id, count) ->
        count > 0 && SHOP_ITEMS.firstOrNull { it.id == id }?.category == ItemCategory.NEED
    }
    val hasToy = state.inventory.any { (id, count) ->
        count > 0 && SHOP_ITEMS.firstOrNull { it.id == id }?.category == ItemCategory.WANT
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_greeting, state.petName)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                CoinChip(coins = state.coins, modifier = Modifier.padding(top = 8.dp))
            }

            PetHeroImage(mood = mood, petName = state.petName, modifier = Modifier.padding(top = 8.dp))

            Text(
                text = when (mood) {
                    PetMood.HAPPY -> stringResource(R.string.home_mood_happy, state.petName)
                    PetMood.NEUTRAL -> stringResource(R.string.home_mood_neutral, state.petName)
                    PetMood.SAD -> stringResource(R.string.home_mood_sad, state.petName)
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
            )

            StatBar(
                label = stringResource(R.string.home_hunger_label),
                emoji = "🍗",
                value = state.hunger,
                color = LeafGreen,
            )
            StatBar(
                label = stringResource(R.string.home_happiness_label),
                emoji = "😊",
                value = state.happiness,
                color = SkyBlue,
                modifier = Modifier.padding(top = 12.dp),
            )

            if (state.streakDays > 0) {
                Text(
                    text = stringResource(R.string.home_streak_label, state.streakDays),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hasFood) {
                            onFeed()
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(itemsNeededMessage(true)) }
                        }
                    },
                ) { Text(stringResource(R.string.home_feed_button)) }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (hasToy) {
                            onPlay()
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(itemsNeededMessage(false)) }
                        }
                    },
                ) { Text(stringResource(R.string.home_play_button)) }
            }
        }
    }
}

private fun itemsNeededMessage(forFood: Boolean): String =
    if (forFood) "Нет еды — загляни в магазин! 🛍️" else "Нет игрушек — загляни в магазин! 🛍️"
