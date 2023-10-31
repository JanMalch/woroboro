package io.github.janmalch.woroboro.ui.exercise.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.theme.LoveRed
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val DurationSaver = Saver<Duration?, String>(
    save = { it?.toIsoString() ?: "" },
    restore = { it.takeUnless(String::isEmpty)?.let(Duration.Companion::parseIsoString) }
)

@Composable
fun ExerciseEditor(
    availableTags: ImmutableMap<String, ImmutableSet<String>>,
    exercise: Exercise?,
    onSave: (Exercise) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val id: UUID = rememberSaveable { exercise?.id ?: UUID.randomUUID() }
    var name: String by rememberSaveable { mutableStateOf(exercise?.name ?: "") }
    var description: String by rememberSaveable { mutableStateOf(exercise?.description ?: "") }
    var tags: List<Tag> by rememberSaveable {
        mutableStateOf(
            ArrayList(exercise?.tags ?: listOf())
        )
    }
    var sets: Int? by rememberSaveable { mutableStateOf(exercise?.sets ?: 3) }
    var reps: Int? by rememberSaveable { mutableStateOf(exercise?.reps) }
    var hold: Duration? by rememberSaveable(stateSaver = DurationSaver) { mutableStateOf(exercise?.hold) }
    var pause: Duration? by rememberSaveable(stateSaver = DurationSaver) { mutableStateOf(exercise?.pause ?: 30.seconds) }
    var isFavorite: Boolean by rememberSaveable { mutableStateOf(exercise?.isFavorite ?: false) }

    Column(modifier = modifier) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = "Name") },
                singleLine = true,
                // isError = name.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = "Beschreibung") },
                singleLine = false,
                // isError = description.isBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(max = 192.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NumberTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    required = true,
                    label = { Text(text = "Sätze", softWrap = false, maxLines = 1) },
                    modifier = Modifier.weight(1F),
                )

                NumberTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    required = false,
                    label = { Text(text = "Wdh.", softWrap = false, maxLines = 1) },
                    modifier = Modifier.weight(1F),
                )

                DurationTextField(
                    value = hold,
                    onValueChange = { hold = it },
                    required = false,
                    label = { Text(text = "Halten", softWrap = false, maxLines = 1) },
                    modifier = Modifier.weight(1F),
                )

                DurationTextField(
                    value = pause,
                    onValueChange = { pause = it },
                    required = false,
                    label = { Text(text = "Pause", softWrap = false, maxLines = 1) },
                    modifier = Modifier.weight(1F),
                    imeAction = ImeAction.Done,
                )
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {


            IsFavoriteCheckbox(
                value = isFavorite,
                onValueChange = { isFavorite = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            TagSelectors(
                availableTags = availableTags,
                value = tags,
                onValueChange = { tags = it }
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = {
                    sets?.let {
                        val edited = Exercise(
                            id = id,
                            name = name.trim(),
                            description = description.trim(),
                            tags = tags.toImmutableList(),
                            sets = it,
                            reps = reps,
                            hold = hold,
                            pause = pause,
                            isFavorite = isFavorite,
                        )
                        onSave(edited)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && description.isNotBlank() && (reps != null || hold != null),
            ) {
                Text(text = "Speichern")
            }

            AnimatedVisibility(
                visible = exercise != null,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        onDelete(id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "Löschen")
                }
            }
        }
    }
}

@Composable
fun IsFavoriteCheckbox(
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onValueChange(!value) }
    ) {
        Crossfade(
            targetState = value,
            label = "Crossfade:Icon:IsFavoriteCheckbox",
        ) {
            Icon(
                if (it) Icons.Rounded.Favorite
                else Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                tint = if (it) LoveRed else LocalContentColor.current,
            )
        }
        Text(text = "Lieblingsübung")
    }
}

@Composable
fun TagSelectors(
    availableTags: ImmutableMap<String, ImmutableSet<String>>,
    value: List<Tag>,
    onValueChange: (List<Tag>) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        availableTags.forEach { (type, labels) ->
            val valuesForType = remember(type, value) {
                value.mapNotNull { it.takeIf { it.type == type }?.label }
            }

            TagTypeMultiDropdown(
                type = type,
                availableLabels = labels,
                value = valuesForType,
                onValueChange = { newValuesForType ->
                    onValueChange(
                        // remove old values for this type
                        value.filter { it.type != type } +
                                // and append new ones
                                (newValuesForType.map { Tag(label = it, type = type) })
                    )
                }
            )
        }

    }
}

@Composable
fun TagTypeMultiDropdown(
    type: String,
    availableLabels: ImmutableSet<String>,
    value: List<String>,
    onValueChange: (List<String>) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(text = type, softWrap = false) },
            colors = if (value.isNotEmpty()) AssistChipDefaults.elevatedAssistChipColors() else AssistChipDefaults.assistChipColors(),
            elevation = if (value.isNotEmpty()) AssistChipDefaults.assistChipElevation(
                elevation = 12.dp,
            ) else null,
            leadingIcon = if (value.isNotEmpty()) {
                {
                    Badge {
                        Text(text = value.size.toString())
                    }
                }
            } else null,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            availableLabels.forEach { availableLabel ->
                var isSelected by remember { mutableStateOf(availableLabel in value) }
                DropdownMenuItem(
                    text = { Text(availableLabel) },
                    onClick = {
                        if (isSelected) {
                            isSelected = false
                            onValueChange(value - availableLabel)
                        } else {
                            isSelected = true
                            onValueChange(value + availableLabel)
                        }
                    },
                    leadingIcon = {
                        Crossfade(
                            targetState = isSelected,
                            label = "Crossfade:Icon:$availableLabel",
                        ) {
                            Icon(
                                painterResource(
                                    id =
                                    if (it) R.drawable.round_check_box_24
                                    else R.drawable.round_check_box_outline_blank_24
                                ),
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NumberTextField(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    required: Boolean = false,
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
        imeAction = imeAction,
    )
}
