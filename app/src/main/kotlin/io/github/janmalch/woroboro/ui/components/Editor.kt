package io.github.janmalch.woroboro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.janmalch.woroboro.utils.findWholeUnit
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun TimeField(
    value: LocalTime?,
    onValueChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = {
        Icon(Icons.Rounded.AccessTime, contentDescription = null)
    },
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    min: LocalTime = LocalTime.MIN,
) {
    val defaultValue = remember { LocalTime.now().truncatedTo(ChronoUnit.HOURS) }
    var isPickerOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val interactions = remember {
        MutableInteractionSource()
    }
    if (interactions.collectIsPressedAsState().value) {
        isPickerOpen = true
    }
    OutlinedTextField(
        value = value?.format(DateTimeFormatter.ofPattern("HH:mm"))?.let { "$it Uhr" } ?: "",
        onValueChange = {},
        label = label,
        singleLine = true,
        isError = (required && value == null) || (value != null && value < min),
        readOnly = true,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        interactionSource = interactions,
    )


    if (isPickerOpen) {
        val state = rememberTimePickerState(
            initialHour = value?.hour ?: defaultValue.hour,
            initialMinute = value?.minute ?: defaultValue.minute,
        )

        AlertDialog(
            onDismissRequest = { isPickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(LocalTime.of(state.hour, state.minute))
                    isPickerOpen = false
                }) {
                    Text(text = "OK")
                }
            },
            text = {
                TimePicker(state = state)
            }
        )
    }
}

@Composable
fun NumberTextField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
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
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
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
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    min: Duration = ONE_SECOND,
    imeAction: ImeAction = ImeAction.Next,
) {
    var unit by remember(value) {
        mutableStateOf(findWholeUnit(value))
    }
    var textValue by remember(value) {
        mutableStateOf(value?.toLong(unit)?.coerceToInt())
    }
    var isDropdownOpen by remember {
        mutableStateOf(false)
    }

    NumberTextField(
        value = textValue,
        onValueChange = {
            textValue = it
            onValueChange(it?.toDuration(unit))
        },
        modifier = modifier,
        required = required,
        min = min.inWholeSeconds.toInt(),
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        imeAction = imeAction,
        suffix = {
            Box {
                Text(
                    text = abbreviationOfDurationUnit(unit),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(13.dp)
                        .clickable { isDropdownOpen = true }
                        .clip(RoundedCornerShape(4.dp))
                )

                DropdownMenu(
                    expanded = isDropdownOpen,
                    onDismissRequest = { isDropdownOpen = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(text = nameOfDurationUnit(DurationUnit.SECONDS))
                        },
                        onClick = {
                            unit = DurationUnit.SECONDS
                            onValueChange(textValue?.toDuration(unit))
                            isDropdownOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(text = nameOfDurationUnit(DurationUnit.MINUTES))
                        },
                        onClick = {
                            unit = DurationUnit.MINUTES
                            onValueChange(textValue?.toDuration(unit))
                            isDropdownOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(text = nameOfDurationUnit(DurationUnit.HOURS))
                        },
                        onClick = {
                            unit = DurationUnit.HOURS
                            onValueChange(textValue?.toDuration(unit))
                            isDropdownOpen = false
                        },
                    )
                }
            }
        }
    )
}

@Composable
@ReadOnlyComposable
fun abbreviationOfDurationUnit(unit: DurationUnit): String {
    return unit.name.lowercase().take(3) // TODO: translate
}

@Composable
@ReadOnlyComposable
fun nameOfDurationUnit(unit: DurationUnit): String {
    return unit.name.lowercase().capitalize(Locale.ROOT) // TODO: translate
}

private fun Long.coerceToInt(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
