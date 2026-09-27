package ru.finnipet.app.domain

import androidx.annotation.DrawableRes
import ru.finnipet.app.R
import ru.finnipet.app.data.GameState

enum class PetPose(@DrawableRes val art: Int) {
    IDLE(R.drawable.pet_idle),
    HAPPY(R.drawable.pet_happy),
    SAD(R.drawable.pet_sad),
    HUNGRY(R.drawable.pet_hungry),
    EATING(R.drawable.pet_eating),
    PLAY(R.drawable.pet_play),
    LOVE(R.drawable.pet_love),
    SLEEP(R.drawable.pet_sleep),
    WAVE(R.drawable.pet_wave),
    THINK(R.drawable.pet_think),
    TEACHER(R.drawable.pet_teacher),
    SHOP(R.drawable.pet_shop),
    PIGGY(R.drawable.pet_piggy),
    GIFT(R.drawable.pet_gift),
    CHEER(R.drawable.pet_cheer),
    OOPS(R.drawable.pet_oops),
    TROPHY(R.drawable.pet_trophy),
}

enum class PetNeed { NONE, HUNGRY, BORED }

const val LOW_STAT = 30f

fun petNeed(state: GameState): PetNeed = when {
    state.hunger < LOW_STAT -> PetNeed.HUNGRY
    state.happiness < LOW_STAT -> PetNeed.BORED
    else -> PetNeed.NONE
}

/** The pose Finni holds when nothing special is happening. */
fun restingPose(state: GameState): PetPose = when (petNeed(state)) {
    PetNeed.HUNGRY -> PetPose.HUNGRY
    PetNeed.BORED -> PetPose.SAD
    PetNeed.NONE -> PetPose.IDLE
}
