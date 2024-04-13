package io.github.janmalch.woroboro.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals

data class SnackbarAction(
    override val message: String,
    override val actionLabel: String,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: suspend (SnackbarResult) -> Unit,
) : SnackbarVisuals
