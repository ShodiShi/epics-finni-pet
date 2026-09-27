package ru.finnipet.app.domain

import androidx.annotation.DrawableRes
import ru.finnipet.app.R

/** Something Finni saves up for. Reaching it puts the item into Finni's room for good. */
data class SavingsGoal(
    val id: String,
    val title: String,
    val roomLine: String,
    @DrawableRes val art: Int,
    val target: Int,
)

val SAVINGS_GOALS: List<SavingsGoal> = listOf(
    SavingsGoal("goal_bed", "Мягкая лежанка", "Теперь у меня есть мягкая лежанка!", R.drawable.goal_bed, 100),
    SavingsGoal("goal_zoo", "Плюшевый лев", "Со мной теперь живёт плюшевый лев!", R.drawable.goal_zoo, 160),
    SavingsGoal("goal_bike", "Велосипед", "У меня есть свой велосипед!", R.drawable.goal_bike, 240),
    SavingsGoal("goal_house", "Уютный домик", "У меня теперь свой домик!", R.drawable.goal_house, 350),
)

fun savingsGoalById(id: String): SavingsGoal = SAVINGS_GOALS.firstOrNull { it.id == id } ?: SAVINGS_GOALS.first()

fun nextSavingsGoal(currentId: String): SavingsGoal {
    val index = SAVINGS_GOALS.indexOfFirst { it.id == currentId }
    return SAVINGS_GOALS[(index + 1).mod(SAVINGS_GOALS.size)]
}

/** A good cause the kid can put coins toward. */
data class ShareGoal(
    val id: String,
    val title: String,
    val description: String,
    val thanks: String,
    @DrawableRes val art: Int,
    val target: Int,
)

val SHARE_GOALS: List<ShareGoal> = listOf(
    ShareGoal("share_food", "Корм для приюта", "Щенки и котята в приюте ждут вкусный обед.",
        "Щенки и котята в приюте сыты и довольны!", R.drawable.share_food, 40),
    ShareGoal("share_books", "Книги для библиотеки", "Новые книжки для детской библиотеки.",
        "Ребята в библиотеке уже читают новые книжки!", R.drawable.share_books, 60),
    ShareGoal("share_tree", "Дерево в парке", "Посадим дерево, чтобы в парке было больше тени.",
        "В парке выросло новое дерево!", R.drawable.share_tree, 80),
)

fun shareGoalById(id: String): ShareGoal = SHARE_GOALS.firstOrNull { it.id == id } ?: SHARE_GOALS.first()

fun nextShareGoal(currentId: String): ShareGoal {
    val index = SHARE_GOALS.indexOfFirst { it.id == currentId }
    return SHARE_GOALS[(index + 1).mod(SHARE_GOALS.size)]
}
