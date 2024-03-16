package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Stable
fun Modifier.toolbarButtonSize() = this.defaultMinSize(
    minWidth = ButtonDefaults.MinWidth,
    minHeight = 36.dp
)
