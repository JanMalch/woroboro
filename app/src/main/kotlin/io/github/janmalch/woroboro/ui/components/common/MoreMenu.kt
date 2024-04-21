package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Immutable
interface MoreMenuScope : ColumnScope {
    fun dismissMoreMenu()
}

private class MoreMenuScopeImpl(
    private val columnScope: ColumnScope,
    private val _dismissMoreMenu: () -> Unit,
) : MoreMenuScope, ColumnScope by columnScope {
    override fun dismissMoreMenu() {
        _dismissMoreMenu()
    }
}

@Composable
fun MoreMenu(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable MoreMenuScope.() -> Unit,
) {
    val clearFocus = rememberClearFocus()
    var isOpen by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            clearFocus()
            isOpen = true
        },
        enabled = enabled
    ) {
        Icon(Icons.Rounded.MoreVert, contentDescription = null)
    }
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = { isOpen = false },
        content = {
            MoreMenuScopeImpl(this) {
                isOpen = false
            }.content()
        },
        modifier = modifier,
    )
}

@Composable
fun MoreMenuScope.MoreMenuItem(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
                text()
            }
        },
        onClick = onClick,
        modifier = modifier,
        trailingIcon = {
            Box(
                modifier = Modifier.padding(start = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                icon()
            }
        },
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp)
    )
}
