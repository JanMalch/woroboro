package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MoreMenu(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isOpen by remember { mutableStateOf(false) }
    IconButton(onClick = { isOpen = true }, enabled = enabled) {
        Icon(Icons.Rounded.MoreVert, contentDescription = null)
    }
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = { isOpen = false },
        content = content,
        modifier = modifier,
    )
}