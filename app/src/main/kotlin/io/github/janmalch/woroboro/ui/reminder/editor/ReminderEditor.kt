package io.github.janmalch.woroboro.ui.reminder.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.RoutineQuery
import io.github.janmalch.woroboro.models.asRoutineFilter
import io.github.janmalch.woroboro.ui.components.DurationTextField
import io.github.janmalch.woroboro.ui.components.TimeField
import io.github.janmalch.woroboro.ui.components.common.ButtonLoading
import io.github.janmalch.woroboro.ui.components.common.CloseIconButton
import io.github.janmalch.woroboro.ui.components.common.MoreMenu
import io.github.janmalch.woroboro.ui.components.common.MoreMenuItem
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
                            val edited = Reminder(
                                id = id,
                                name = name.trim(),
                                weekdays = weekdays.toSet(),
                                remindAt = remindAt,
                                repeat = if (repeatEverySnapshot != null && repeatUntilSnapshot != null)
                                    Reminder.Repeat(repeatEverySnapshot, repeatUntilSnapshot)
                                else null,
                                query = RoutineQuery.RoutineFilter(
                                    onlyFavorites = onlyFavorites,
                                    durationFilter = durationFilter,
                                    selectedTags = selectedTags,
                                ),
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
                        modifier = Modifier.defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = 36.dp
                        )
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
                text = stringResource(R.string.routine_filter),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            RoutineFilterRow(
                availableTags = availableTags,
                selectedTags = selectedTags.toImmutableList(),
                isOnlyFavorites = onlyFavorites,
                durationFilter = durationFilter,
                onDurationFilterChange = { durationFilter = it },
                onOnlyFavoritesChange = { onlyFavorites = it },
                onSelectedTagsChange = {
                    selectedTags.clear()
                    selectedTags.addAll(it)
                },
                contentPadding = PaddingValues(horizontal = 24.dp)
            )
        }

    }
}