package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun BackIconButton(
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
    }
}

@Composable
fun CloseIconButton(
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(Icons.Rounded.Close, contentDescription = null)
    }
}