package ru.finnipet.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.finnipet.app.data.GameState
import ru.finnipet.app.ui.theme.Cream
import ru.finnipet.app.ui.theme.LineStrong

/**
 * Bottom sheets live in the activity's own window (not a dialog window), so reward particles can fly over them.
 * Content receives the latest state on every recomposition.
 */
class SheetHost {
    var content: (@Composable (GameState) -> Unit)? by mutableStateOf(null)
        private set

    fun show(content: @Composable (GameState) -> Unit) {
        this.content = content
    }

    fun hide() {
        content = null
    }
}

val LocalSheets = staticCompositionLocalOf { SheetHost() }

@Composable
fun SheetLayer(host: SheetHost, state: GameState) {
    val current = host.content
    var last by remember { mutableStateOf(current) }
    if (current != null) last = current

    BackHandler(enabled = current != null) { host.hide() }

    AnimatedVisibility(current != null, enter = fadeIn(tween(180)), exit = fadeOut(tween(200))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x992A1D15))
                .clickable(remember { MutableInteractionSource() }, indication = null) { host.hide() }
        )
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            current != null,
            enter = slideInVertically(spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)) { it },
            exit = slideOutVertically(tween(220)) { it },
        ) {
            val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.86f)
                    .wrapContentHeight(Alignment.Bottom)
                    .shadow(20.dp, shape)
                    .background(Cream, shape)
                    .clickable(remember { MutableInteractionSource() }, indication = null) { }
                    .navigationBarsPadding()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(width = 44.dp, height = 5.dp)
                        .background(LineStrong, RoundedCornerShape(50))
                )
                last?.invoke(state)
            }
        }
    }
}
