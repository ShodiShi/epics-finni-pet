package ru.finnipet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.savingsGoalById

@Composable
fun SavingsScreen(
    state: GameState,
    onDeposit: (Int) -> Unit,
    onNewGoal: () -> Unit,
    onAcknowledgeInterest: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val goal = savingsGoalById(state.savingsGoalId)
    val reached = state.savingsBalance >= goal.target

    LaunchedEffect(state.lastInterestEarned) {
        if (state.lastInterestEarned > 0) {
            snackbarHostState.showSnackbar("+${state.lastInterestEarned} 🌱 " + "монет процентов")
            onAcknowledgeInterest()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.savings_title), style = MaterialTheme.typography.headlineMedium)

            Text(goal.emoji, style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(top = 16.dp))
            Text(
                text = stringResource(R.string.savings_goal_label, goal.title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(
                    R.string.savings_balance_format,
                    state.savingsBalance.coerceAtMost(goal.target),
                    goal.target,
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp),
            )

            LinearProgressIndicator(
                progress = { (state.savingsBalance.toFloat() / goal.target).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            if (reached) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.savings_goal_reached_title), style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = stringResource(R.string.savings_goal_reached_body, goal.title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Button(onClick = onNewGoal, modifier = Modifier.padding(top = 12.dp)) {
                            Text(stringResource(R.string.savings_new_goal_button))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { onDeposit(10) },
                        enabled = state.coins >= 10,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.savings_add_button, 10)) }

                    Button(
                        onClick = { onDeposit(state.coins) },
                        enabled = state.coins > 0,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.savings_add_all_button)) }
                }
            }

            Text(
                text = stringResource(R.string.savings_explainer),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
