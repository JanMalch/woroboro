package io.github.janmalch.woroboro.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun NumberTextField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    min: Int = 1,
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value = value?.toString(10) ?: "",
        onValueChange = {
            if (it.isBlank()) {
                onValueChange(null)
            } else {
                val parsed = it.toIntOrNull()
                if (parsed != null) {
                    onValueChange(parsed)
                }
            }
        },
        label = label,
        singleLine = true,
        isError = (value != null && value < min) || (required && value == null),
        modifier = modifier,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        )
    )
}

private val ONE_SECOND = 1.seconds

@Composable
fun DurationTextField(
    value: Duration?,
    onValueChange: (Duration?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    min: Duration = ONE_SECOND,
    imeAction: ImeAction = ImeAction.Next,
) {
    NumberTextField(
        value = value?.inWholeSeconds?.toInt(),
        onValueChange = { onValueChange(it?.seconds) },
        modifier = modifier,
        required = required,
        min = min.inWholeSeconds.toInt(),
        label = label,
        enabled = enabled,
        imeAction = imeAction,
    )
}
