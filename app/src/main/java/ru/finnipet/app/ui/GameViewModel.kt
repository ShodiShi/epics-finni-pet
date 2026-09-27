package ru.finnipet.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.finnipet.app.data.GameRepository
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.Badge
import ru.finnipet.app.domain.SavingsGoal
import ru.finnipet.app.domain.ShareGoal
import ru.finnipet.app.domain.applyBadgeUnlocks
import ru.finnipet.app.domain.badge
import ru.finnipet.app.domain.levelForXp
import ru.finnipet.app.domain.savingsGoalById
import ru.finnipet.app.domain.shareGoalById
import ru.finnipet.app.domain.tick
import ru.finnipet.app.domain.answerQuestion as answerQuestionRule
import ru.finnipet.app.domain.answerSortCard as answerSortCardRule
import ru.finnipet.app.domain.buyItem as buyItemRule
import ru.finnipet.app.domain.claimChest as claimChestRule
import ru.finnipet.app.domain.deposit as depositRule
import ru.finnipet.app.domain.finishSortRound as finishSortRoundRule
import ru.finnipet.app.domain.petThePet as petRule
import ru.finnipet.app.domain.share as shareRule
import ru.finnipet.app.domain.useItem as useItemRule

/** Moments worth a full-screen celebration. */
sealed interface GameEvent {
    data class BadgeUnlocked(val badge: Badge) : GameEvent
    data class LevelUp(val level: Int) : GameEvent
    data class GoalReached(val goal: SavingsGoal) : GameEvent
    data class ShareGoalReached(val goal: ShareGoal) : GameEvent
}

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    val state: StateFlow<GameState?> =
        repository.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<GameEvent> = _events

    init {
        viewModelScope.launch {
            while (true) {
                mutate { tick(it, System.currentTimeMillis()) }
                delay(CLOCK_STEP_MILLIS)
            }
        }
    }

    private fun mutate(rule: (GameState) -> GameState) {
        viewModelScope.launch {
            val (before, after) = repository.update { applyBadgeUnlocks(rule(it)) }
            announce(before, after)
        }
    }

    private suspend fun announce(before: GameState, after: GameState) {
        if (!before.onboardingComplete || !after.onboardingComplete) return
        if (after.goalsReached > before.goalsReached) {
            after.lastReachedGoalId?.let { _events.emit(GameEvent.GoalReached(savingsGoalById(it))) }
        }
        if (after.completedShareGoals > before.completedShareGoals) {
            after.lastCompletedShareGoalId?.let { _events.emit(GameEvent.ShareGoalReached(shareGoalById(it))) }
        }
        (after.unlockedBadgeIds - before.unlockedBadgeIds).mapNotNull(::badge).forEach {
            _events.emit(GameEvent.BadgeUnlocked(it))
        }
        val level = levelForXp(after.xp)
        if (level > levelForXp(before.xp)) _events.emit(GameEvent.LevelUp(level))
    }

    fun completeOnboarding(name: String) = mutate {
        it.copy(onboardingComplete = true, petName = name.trim().ifBlank { it.petName })
    }

    fun renamePet(name: String) = mutate { it.copy(petName = name.trim().ifBlank { it.petName }) }
    fun setSound(on: Boolean) = mutate { it.copy(soundOn = on) }
    fun setMusic(on: Boolean) = mutate { it.copy(musicOn = on) }

    fun answerQuestion(questionId: String, chosenIndex: Int) = mutate { answerQuestionRule(it, questionId, chosenIndex) }
    fun answerSortCard(correct: Boolean) = mutate { answerSortCardRule(it, correct) }
    fun finishSortRound(score: Int, total: Int) = mutate { finishSortRoundRule(it, score, total) }

    fun buyItem(itemId: String) = mutate { buyItemRule(it, itemId) }
    fun useItem(itemId: String) = mutate { useItemRule(it, itemId) }
    fun petThePet() = mutate(::petRule)

    fun deposit(amount: Int) = mutate { depositRule(it, amount) }
    fun share(amount: Int) = mutate { shareRule(it, amount) }
    fun acknowledgeInterest() = mutate { it.copy(lastInterestEarned = 0) }
    fun claimChest() = mutate(::claimChestRule)

    // Hidden demo tools for presenting the game.
    fun demoHungry() = mutate { it.copy(hunger = 18f) }
    fun demoBored() = mutate { it.copy(happiness = 18f) }
    fun demoCoins() = mutate { it.copy(coins = it.coins + 100) }
    fun demoNextDay() = mutate { tick(it.copy(debugDayOffset = it.debugDayOffset + 1), System.currentTimeMillis()) }

    fun resetProgress() {
        viewModelScope.launch { repository.reset() }
    }

    companion object {
        private const val CLOCK_STEP_MILLIS = 30_000L

        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GameViewModel(GameRepository(context.applicationContext)) as T
            }
    }
}
