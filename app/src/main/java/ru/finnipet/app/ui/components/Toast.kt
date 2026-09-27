package ru.finnipet.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.finnipet.app.ui.theme.Ink
import ru.finnipet.app.ui.theme.Line
import ru.finnipet.app.ui.theme.Paper

data class ToastMessage(
    val text: String,
    @DrawableRes val icon: Int? = null,
    val action: String? = null,
    val onAction: (() -> Unit)? = null,
    val serial: Long = System.nanoTime(),
)

@Stable
class ToastState {
    var current: ToastMessage? by mutableStateOf(null)
        private set

    fun show(text: String, @DrawableRes icon: Int? = null, action: String? = null, onAction: (() -> Unit)? = null) {
        current = ToastMessage(text, icon, action, onAction)
    }

    fun dismiss() {
        current = null
    }
}

@Composable
fun rememberToastState() = remember { ToastState() }

/** Pill that drops from the top of the screen and leaves on its own. */
@Composable
fun ToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val message = state.current
    var last by remember { mutableStateOf(message) }
    if (message != null) last = message
    LaunchedEffect(message?.serial) {
        if (message != null) {
            delay(if (message.action != null) 3200 else 2200)
            state.dismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = slideInVertically(spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) { -it } + fadeIn(),
        exit = slideOutVertically(tween(200)) { -it } + fadeOut(),
    ) {
        val shown = last ?: return@AnimatedVisibility
        Row(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x44000000), spotColor = Color(0x44000000))
                .background(Paper, RoundedCornerShape(22.dp))
                .border(2.dp, Line, RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (shown.icon != null) {
                Image(painterResource(shown.icon), null, Modifier.size(34.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(shown.text, style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.weight(1f))
            if (shown.action != null && shown.onAction != null) {
                Spacer(Modifier.width(8.dp))
                ChunkyButton(
                    shown.action,
                    onClick = {
                        state.dismiss()
                        shown.onAction.invoke()
                    },
                    color = ChunkyColor.ORANGE,
                    height = 38.dp,
                    textStyle = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
