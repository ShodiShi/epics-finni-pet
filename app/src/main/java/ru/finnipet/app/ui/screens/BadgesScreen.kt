package ru.finnipet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.BADGES
import ru.finnipet.app.domain.levelForXp
import ru.finnipet.app.domain.levelProgress

@Composable
fun BadgesScreen(state: GameState) {
    val level = levelForXp(state.xp)
    val (into, needed) = levelProgress(state.xp)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(R.string.badges_title), style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.badges_level_label, level), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.badges_level_progress, into, needed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { into.toFloat() / needed },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            items(BADGES) { badge ->
                val unlocked = badge.id in state.unlockedBadgeIds
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (unlocked) 1f else 0.5f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unlocked) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(badge.emoji, style = MaterialTheme.typography.headlineMedium)
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(badge.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (unlocked) badge.description else stringResource(R.string.badges_locked),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
