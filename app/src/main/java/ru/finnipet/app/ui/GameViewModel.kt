package ru.finnipet.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.finnipet.app.data.GameRepository
import ru.finnipet.app.data.GameState
import ru.finnipet.app.domain.ItemCategory
import ru.finnipet.app.domain.Question
import ru.finnipet.app.domain.SHOP_ITEMS
import ru.finnipet.app.domain.applyBadgeUnlocks
import ru.finnipet.app.domain.applyDayRollover
import ru.finnipet.app.domain.nextSavingsGoal
import ru.finnipet.app.domain.todaysQuestions

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    val uiState: StateFlow<GameState> =
        repository.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GameState())

    init {
        // Corrects the persisted state as soon as a new calendar day is observed.
        viewModelScope.launch {
            repository.state.collect { state ->
                val rolled = applyDayRollover(state)
                if (rolled != state) {
                    repository.update { applyDayRollover(it) }
                }
            }
        }
    }

    private fun mutate(block: (GameState) -> GameState) {
        viewModelScope.launch { repository.update { applyBadgeUnlocks(block(it)) } }
    }

    fun completeOnboarding(name: String) = mutate { state ->
        state.copy(onboardingComplete = true, petName = name.ifBlank { state.petName })
    }

    fun renamePet(name: String) = mutate { state ->
        state.copy(petName = name.ifBlank { state.petName })
    }

    fun acknowledgeInterest() = mutate { state -> state.copy(lastInterestEarned = 0) }

    fun answerQuestion(question: Question, chosenIndex: Int) = mutate { state ->
        if (question.id in state.currentDayAnsweredIds) return@mutate state
        val correct = chosenIndex == question.correctIndex
        val reward = if (correct) question.reward else 0
        val afterAnswer = state.copy(
            coins = state.coins + reward,
            coinsEarnedToday = state.coinsEarnedToday + reward,
            xp = state.xp + reward,
            currentDayAnsweredIds = state.currentDayAnsweredIds + question.id,
            allTimeAnsweredIds = state.allTimeAnsweredIds + question.id,
        )
        val todaysBatch = todaysQuestions(state.currentDayEpoch)
        val completedAllToday = todaysBatch.all { it.id in afterAnswer.currentDayAnsweredIds }
        if (completedAllToday && state.lastStreakCreditEpoch != state.currentDayEpoch) {
            afterAnswer.copy(
                streakDays = afterAnswer.streakDays + 1,
                lastStreakCreditEpoch = state.currentDayEpoch,
            )
        } else {
            afterAnswer
        }
    }

    fun buyItem(itemId: String) = mutate { state ->
        val item = SHOP_ITEMS.firstOrNull { it.id == itemId } ?: return@mutate state
        if (state.coins < item.cost) return@mutate state
        state.copy(
            coins = state.coins - item.cost,
            inventory = state.inventory + (itemId to (state.inventory[itemId] ?: 0) + 1),
            totalNeedPurchases = state.totalNeedPurchases + if (item.category == ItemCategory.NEED) 1 else 0,
            totalWantPurchases = state.totalWantPurchases + if (item.category == ItemCategory.WANT) 1 else 0,
        )
    }

    fun feedPet() = mutate { state -> consumeFirstAvailable(state, ItemCategory.NEED, restoresHunger = true) }

    fun playWithPet() = mutate { state -> consumeFirstAvailable(state, ItemCategory.WANT, restoresHunger = false) }

    private fun consumeFirstAvailable(state: GameState, category: ItemCategory, restoresHunger: Boolean): GameState {
        val availableId = state.inventory.entries.firstOrNull { (id, count) ->
            count > 0 && SHOP_ITEMS.firstOrNull { it.id == id }?.category == category
        }?.key ?: return state
        val item = SHOP_ITEMS.first { it.id == availableId }
        val newInventory = state.inventory.toMutableMap()
        val remaining = (newInventory[availableId] ?: 1) - 1
        if (remaining <= 0) newInventory.remove(availableId) else newInventory[availableId] = remaining
        return if (restoresHunger) {
            state.copy(inventory = newInventory, hunger = (state.hunger + item.statGain).coerceAtMost(100))
        } else {
            state.copy(inventory = newInventory, happiness = (state.happiness + item.statGain).coerceAtMost(100))
        }
    }

    fun depositToSavings(amount: Int) = mutate { state ->
        val actual = amount.coerceIn(0, state.coins)
        state.copy(coins = state.coins - actual, savingsBalance = state.savingsBalance + actual)
    }

    fun chooseNextSavingsGoal() = mutate { state ->
        state.copy(savingsGoalId = nextSavingsGoal(state.savingsGoalId).id, savingsBalance = 0)
    }

    fun resetProgress() {
        viewModelScope.launch { repository.reset() }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameViewModel(GameRepository(context.applicationContext)) as T
                }
            }
    }
}
