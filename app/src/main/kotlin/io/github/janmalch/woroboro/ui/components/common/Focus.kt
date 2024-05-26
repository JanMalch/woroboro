package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalFocusManager

fun Modifier.clearFocusAsOutsideClick(force: Boolean = true) = composed {
    val focusManager = LocalFocusManager.current
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { focusManager.clearFocus(force = force) }
    )
}

fun Modifier.clickableWithClearFocus(
    force: Boolean = true,
    onClick: () -> Unit,
) = composed {
    val focusManager = LocalFocusManager.current
    Modifier.clickable {
        focusManager.clearFocus(force = force)
        onClick()
    }
}

@Composable
fun rememberClearFocus(
    force: Boolean = true,
): () -> Unit {
    val focusManager = LocalFocusManager.current
    return remember(focusManager) { { focusManager.clearFocus(force = force) } }
}
