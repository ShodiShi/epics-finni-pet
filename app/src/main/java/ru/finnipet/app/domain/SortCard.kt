package ru.finnipet.app.domain

import androidx.annotation.DrawableRes
import ru.finnipet.app.R

/** A card in the "Нужно или хочется?" game. */
data class SortCard(
    val id: String,
    val name: String,
    @DrawableRes val art: Int,
    val isNeed: Boolean,
    val hint: String,
)

val SORT_CARDS: List<SortCard> = listOf(
    SortCard("sort_water", "Вода", R.drawable.sort_water, true, "Без воды не прожить и дня — это нужно."),
    SortCard("sort_jacket", "Тёплая куртка", R.drawable.sort_jacket, true, "Зимой без куртки замёрзнешь — это нужно."),
    SortCard("sort_medicine", "Лекарство", R.drawable.sort_medicine, true, "Лекарство помогает выздороветь — это нужно."),
    SortCard("sort_toothbrush", "Зубная щётка", R.drawable.sort_toothbrush, true, "Чистить зубы нужно каждый день."),
    SortCard("sort_backpack", "Школьный рюкзак", R.drawable.sort_backpack, true, "В чём-то надо носить учебники — это нужно."),
    SortCard("sort_shoes", "Кроссовки", R.drawable.sort_shoes, true, "Без обуви далеко не уйдёшь — это нужно."),
    SortCard("item_bread", "Хлеб", R.drawable.item_bread, true, "Еда нужна каждый день."),
    SortCard("item_milk", "Молоко", R.drawable.item_milk, true, "Еда нужна каждый день."),
    SortCard("item_apple", "Яблоко", R.drawable.item_apple, true, "Фрукты — полезная еда, это нужно."),
    SortCard("sort_candy", "Леденец", R.drawable.sort_candy, false, "Сладкое приятно, но без него можно — это хочется."),
    SortCard("sort_car", "Игрушечная машинка", R.drawable.sort_car, false, "Игрушка радует, но без неё можно — это хочется."),
    SortCard("sort_gamepad", "Игровая приставка", R.drawable.sort_gamepad, false, "Играть весело, но это не обязательно — это хочется."),
    SortCard("sort_icecream", "Мороженое", R.drawable.sort_icecream, false, "Вкусно, но можно и без него — это хочется."),
    SortCard("sort_stickers", "Наклейки", R.drawable.sort_stickers, false, "Наклейки — это приятно, но не обязательно."),
    SortCard("sort_robot", "Робот", R.drawable.sort_robot, false, "Классная игрушка, но это «хочется»."),
    SortCard("item_balloon", "Воздушный шарик", R.drawable.item_balloon, false, "Шарик — для радости, это хочется."),
    SortCard("item_bear", "Плюшевый мишка", R.drawable.item_bear, false, "Мишка милый, но можно и без него — это хочется."),
    SortCard("item_cap", "Модная кепка", R.drawable.item_cap, false, "Ещё одна кепка — это хочется."),
)

fun sortCard(id: String): SortCard? = SORT_CARDS.firstOrNull { it.id == id }
