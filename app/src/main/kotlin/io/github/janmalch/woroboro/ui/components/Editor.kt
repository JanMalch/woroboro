package io.github.janmalch.woroboro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.CustomExerciseExecution
import io.github.janmalch.woroboro.models.ExerciseExecution
import io.github.janmalch.woroboro.ui.exercise.editor.DurationSaver
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@Composable
fun ExerciseExecutionEditor(
    value: ExerciseExecution?,
    onValueChange: (ExerciseExecution?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSetsRequired: Boolean = true,
) {
    var sets: Int? by rememberSaveable(value) { mutableStateOf(value?.sets) }
    var reps: Int? by rememberSaveable(value) { mutableStateOf(value?.reps) }
    var hold: Duration? by rememberSaveable(value, stateSaver = DurationSaver) {
        mutableStateOf(value?.hold)
    }
    var pause: Duration? by rememberSaveable(value, stateSaver = DurationSaver) {
        mutableStateOf(value?.pause)
    }

    fun emitChange() {
        val execution = sets?.let {
            ExerciseExecution(
                sets = it, reps, hold, pause
            )
        }
        onValueChange(execution)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumberTextField(
            value = sets,
            onValueChange = {
                sets = it
                emitChange()
            },
            required = isSetsRequired || reps != null || hold != null || pause != null,
            label = { Text(text = "Sätze", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            enabled = enabled,
        )

        NumberTextField(
            value = reps,
            onValueChange = {
                reps = it
                emitChange()
            },
            required = false,
            label = { Text(text = "Wdh.", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            enabled = enabled,
        )

        DurationTextField(
            value = hold,
            onValueChange = {
                hold = it
                emitChange()
            },
            required = false,
            label = { Text(text = "Halten", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            enabled = enabled,
        )

        DurationTextField(
            value = pause,
            onValueChange = {
                pause = it
                emitChange()
            },
            required = false,
            label = { Text(text = "Pause", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            imeAction = ImeAction.Done,
            enabled = enabled,
        )
    }
}

@Composable
fun CustomExerciseExecutionEditor(
    value: CustomExerciseExecution?,
    basedOn: ExerciseExecution?,
    onValueChange: (CustomExerciseExecution) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var sets: Int? by rememberSaveable(value) { mutableStateOf(value?.sets) }
    var reps: Int? by rememberSaveable(value) { mutableStateOf(value?.reps) }
    var hold: Duration? by rememberSaveable(value, stateSaver = DurationSaver) {
        mutableStateOf(value?.hold)
    }
    var pause: Duration? by rememberSaveable(value, stateSaver = DurationSaver) {
        mutableStateOf(value?.pause)
    }

    fun emitChange() {
        onValueChange(
            CustomExerciseExecution(
                sets ?: basedOn?.sets, reps, hold, pause
            )
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NumberTextField(
            value = sets,
            onValueChange = {
                sets = it
                emitChange()
            },
            required = value != null,
            label = { Text(text = "Sätze", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            enabled = enabled,
        )

        if (basedOn?.reps != null) {
            NumberTextField(
                value = reps,
                onValueChange = {
                    reps = it
                    emitChange()
                },
                required = false,
                label = { Text(text = "Wdh.", softWrap = false, maxLines = 1) },
                modifier = Modifier.weight(1F),
                enabled = enabled,
            )
        }

        if (basedOn?.hold != null) {
            DurationTextField(
                value = hold,
                onValueChange = {
                    hold = it
                    emitChange()
                },
                required = false,
                label = { Text(text = "Halten", softWrap = false, maxLines = 1) },
                modifier = Modifier.weight(1F),
                enabled = enabled,
            )
        }

        DurationTextField(
            value = pause,
            onValueChange = {
                pause = it
                emitChange()
            },
            required = false,
            label = { Text(text = "Pause", softWrap = false, maxLines = 1) },
            modifier = Modifier.weight(1F),
            imeAction = ImeAction.Done,
            enabled = enabled,
        )
    }
}

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
