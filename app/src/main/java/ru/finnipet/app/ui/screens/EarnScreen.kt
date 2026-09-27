package ru.finnipet.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.Question
import ru.finnipet.app.domain.todaysQuestions
import ru.finnipet.app.ui.components.EmptyState

@Composable
fun EarnScreen(state: GameState, onAnswer: (Question, Int) -> Unit) {
    val todaysBatch = remember(state.currentDayEpoch) { todaysQuestions(state.currentDayEpoch) }
    val alreadyAnswered = remember(state.currentDayEpoch) {
        todaysBatch.count { it.id in state.currentDayAnsweredIds }
    }
    var displayIndex by remember(state.currentDayEpoch) { mutableIntStateOf(alreadyAnswered) }
    val question = todaysBatch.getOrNull(displayIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        if (question == null) {
            EmptyState(
                emoji = "🎉",
                title = stringResource(R.string.earn_done_title),
                body = stringResource(R.string.earn_done_body, state.coinsEarnedToday),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(stringResource(R.string.earn_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.earn_progress, displayIndex + 1, todaysBatch.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            QuestionCard(
                question = question,
                onAnswered = { index -> onAnswer(question, index) },
                onNext = { displayIndex += 1 },
            )
        }
    }
}

@Composable
private fun QuestionCard(question: Question, onAnswered: (Int) -> Unit, onNext: () -> Unit) {
    var answeredIndex by remember(question.id) { mutableStateOf<Int?>(null) }

    Text(text = question.text, style = MaterialTheme.typography.titleLarge)

    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        question.options.forEachIndexed { index, option ->
            val chosen = answeredIndex
            val containerColor = when {
                chosen == null -> MaterialTheme.colorScheme.surfaceVariant
                index == question.correctIndex -> MaterialTheme.colorScheme.secondaryContainer
                index == chosen -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = chosen == null) {
                        answeredIndex = index
                        onAnswered(index)
                    },
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

    if (answeredIndex != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.earn_explanation_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text(stringResource(R.string.earn_next_button))
        }
    }
}
