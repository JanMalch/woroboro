package io.github.janmalch.woroboro.ui.reminder.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.RoutineQuery
import io.github.janmalch.woroboro.models.RoutinesOrder
import io.github.janmalch.woroboro.models.asRoutineFilter
import io.github.janmalch.woroboro.ui.components.DurationTextField
import io.github.janmalch.woroboro.ui.components.TimeField
import io.github.janmalch.woroboro.ui.components.common.ButtonLoading
import io.github.janmalch.woroboro.ui.components.common.CloseIconButton
import io.github.janmalch.woroboro.ui.components.common.MoreMenu
import io.github.janmalch.woroboro.ui.components.common.MoreMenuItem
import io.github.janmalch.woroboro.ui.components.common.clearFocusAsOutsideClick
import io.github.janmalch.woroboro.ui.components.common.toolbarButtonSize
import io.github.janmalch.woroboro.ui.components.routines.RoutineFilterRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

@Composable
fun ReminderEditorScreen(
    isLoading: Boolean,
    reminder: Reminder?,
    routines: ImmutableList<Routine>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    onSave: (Reminder) -> Unit,
    onDelete: (UUID) -> Unit,
    onBackClick: () -> Unit,
) {
    val id = rememberSaveable(reminder) { reminder?.id ?: UUID.randomUUID() }
    var name by rememberSaveable(reminder) { mutableStateOf(reminder?.name ?: "") }
    val weekdays = remember(reminder) {
        mutableStateListOf(
            *(reminder?.weekdays?.toTypedArray() ?: DayOfWeek.values())
        )
    }
    var remindAt by remember(reminder) {
        mutableStateOf(
            reminder?.remindAt ?: LocalTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1)
        )
    }
    var repeatUntil by remember(reminder) { mutableStateOf(reminder?.repeat?.until) }
    var repeatEvery by remember(reminder) { mutableStateOf(reminder?.repeat?.every) }
    var onlyFavorites by rememberSaveable(reminder) {
        mutableStateOf(
            reminder?.query?.asRoutineFilter()?.onlyFavorites ?: false
        )
    }
    var durationFilter by remember(reminder) {
        mutableStateOf(
            reminder?.query?.asRoutineFilter()?.durationFilter ?: DurationFilter.Any
        )
    }
    var routinesOrder by remember(reminder) {
        mutableStateOf(
            reminder?.query?.asRoutineFilter()?.routinesOrder ?: RoutinesOrder.NameAsc
        )
    }
    var routineIdFilter by remember(reminder) {
        mutableStateOf((reminder?.query as? RoutineQuery.Single)?.routineId)
    }
    val selectedTags = remember(reminder) {
        mutableStateListOf(
            *(reminder?.query?.asRoutineFilter()?.selectedTags?.toTypedArray() ?: emptyArray())
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { CloseIconButton(onClick = onBackClick) },
                title = { },
                actions = {

                    Button(
                        onClick = {
                            val repeatEverySnapshot = repeatEvery
                            val repeatUntilSnapshot = repeatUntil
                            val routineIdFilterSnapshot = routineIdFilter
                            val edited = Reminder(
                                id = id,
                                name = name.trim(),
                                weekdays = weekdays.toSet(),
                                remindAt = remindAt,
                                repeat = if (repeatEverySnapshot != null && repeatUntilSnapshot != null)
                                    Reminder.Repeat(repeatEverySnapshot, repeatUntilSnapshot)
                                else null,
                                query = if (routineIdFilterSnapshot != null) {
                                    RoutineQuery.Single(routineIdFilterSnapshot)
                                } else {
                                    RoutineQuery.RoutineFilter(
                                        onlyFavorites = onlyFavorites,
                                        durationFilter = durationFilter,
                                        selectedTags = selectedTags,
                                        routinesOrder = routinesOrder,
                                    )
                                },
                            )
                            onSave(edited)
                        },
                        enabled = !isLoading &&
                                name.isNotBlank() &&
                                weekdays.isNotEmpty() &&
                                (repeatUntil?.let { it > remindAt } ?: true) &&
                                (repeatEvery?.let { it in Reminder.Repeat.VALID_REPEAT_RANGE }
                                    ?: true),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.toolbarButtonSize()
                    ) {
                        ButtonLoading(isVisible = isLoading)
                        Text(text = stringResource(R.string.save))
                    }
                    MoreMenu(enabled = !isLoading && reminder != null) {
                        MoreMenuItem(
                            text = { Text(text = stringResource(R.string.delete_reminder)) },
                            icon = {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                )
                            },
                            onClick = { if (reminder != null) onDelete(reminder.id) }
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .clearFocusAsOutsideClick()
                .padding(padding)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp),
            )

            Text(
                text = stringResource(R.string.reminders),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                DayOfWeek.entries.forEach { dow ->
                    IconToggleButton(
                        checked = dow in weekdays,
                        onCheckedChange = {
                            if (it) {
                                weekdays.add(dow)
                            } else {
                                weekdays.remove(dow)
                            }
                        },
                        colors = IconButtonDefaults.filledIconToggleButtonColors()
                    ) {
                        Text(
                            text = dow.getDisplayName(
                                TextStyle.NARROW,
                                Locale.getDefault()
                            )
                        )
                    }
                }
            }

            TimeField(
                value = remindAt,
                onValueChange = { remindAt = it },
                label = { Text(text = stringResource(R.string.remind_at_input_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )

            DurationTextField(
                value = repeatEvery,
                onValueChange = {
                    repeatEvery = it
                    if (it == null) {
                        repeatUntil = null
                    }
                },
                required = repeatUntil != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                min = 15.minutes,
                label = { Text(text = stringResource(R.string.repeat_every_input_label)) },
                leadingIcon = { Icon(Icons.Rounded.HourglassEmpty, contentDescription = null) }
            )

            AnimatedVisibility(visible = repeatEvery != null) {
                TimeField(
                    value = repeatUntil,
                    onValueChange = { repeatUntil = it },
                    label = { Text(text = stringResource(R.string.repeat_until_input_label)) },
                    required = repeatEvery != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    min = remindAt.plusMinutes(15)
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.routine_filters),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            RoutineFilterRow(
                availableTags = availableTags,
                selectedTags = selectedTags.toImmutableList(),
                isOnlyFavorites = onlyFavorites,
                durationFilter = durationFilter,
                routinesOrder = routinesOrder,
                onRoutinesOrderChange = { routinesOrder = it },
                onDurationFilterChange = { durationFilter = it },
                onOnlyFavoritesChange = { onlyFavorites = it },
                onSelectedTagsChange = {
                    selectedTags.clear()
                    selectedTags.addAll(it)
                },
                contentPadding = PaddingValues(horizontal = 24.dp),
                enabled = routineIdFilter == null,
            )

            Spacer(modifier = Modifier.height(8.dp))

            RoutineSelect(
                routines = routines,
                value = routineIdFilter,
                onValueChange = { routineIdFilter = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )
        }

    }
}

@Composable
private fun RoutineSelect(
    routines: ImmutableList<Routine>,
    value: UUID?,
    onValueChange: (UUID?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }
    val interactions = remember {
        MutableInteractionSource()
    }
    var selectedValue by remember(value) {
        mutableStateOf(value)
    }

    if (interactions.collectIsPressedAsState().value) {
        isDialogOpen = true
    }
    OutlinedTextField(
        value = routines.find { it.id == value }?.name ?: "",
        onValueChange = {},
        label = {
            Text(text = stringResource(R.string.routine_filter_single))
        },
        singleLine = true,
        readOnly = true,
        leadingIcon = {
            Icon(Icons.Rounded.FitnessCenter, contentDescription = null)
        },
        trailingIcon = {
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
        },
        interactionSource = interactions,
        modifier = modifier,
    )

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                isDialogOpen = false
            },
            title = {
                Text(text = stringResource(R.string.routine_filter_single))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                ) {
                    HorizontalDivider()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                    ) {
                        items(routines, key = { it.id }) { routine ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedValue = if (selectedValue == routine.id) {
                                            null
                                        } else {
                                            routine.id
                                        }
                                    }
                            ) {
                                Checkbox(
                                    checked = routine.id == selectedValue,
                                    onCheckedChange = {
                                        selectedValue = if (selectedValue == routine.id) {
                                            null
                                        } else {
                                            routine.id
                                        }
                                    })
                                Text(text = routine.name)
                            }
                        }
                    }
                    HorizontalDivider()

                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(selectedValue)
                    isDialogOpen = false
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
        )
    }
}
