package io.github.janmalch.woroboro.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.RoutineFilter
import io.github.janmalch.woroboro.ui.components.DurationTextField
import io.github.janmalch.woroboro.ui.components.routines.RoutineFilterRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun ReminderListScreen(
    reminders: ImmutableList<Reminder>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    onInsert: (Reminder) -> Unit,
    onUpdate: (Reminder) -> Unit,
    onDelete: (UUID) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Erinnerungen") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                },
                text = {
                    Text(text = "Neue Erinnerung")
                },
                onClick = {
                    onInsert(
                        Reminder(
                            id = UUID.randomUUID(),
                            name = "Test Reminder",
                            weekdays = DayOfWeek.entries.toSet(),
                            remindAt = LocalTime.of(15, 0),
                            repeat = Reminder.Repeat(
                                every = 1.hours,
                                until = LocalTime.of(20, 0),
                            ),
                            filter = RoutineFilter(
                                onlyFavorites = false,
                                durationFilter = DurationFilter.VeryShort,
                                selectedTags = emptyList(),
                            )
                        )
                    )
                },
            )
        },
    ) { padding ->
        ReminderList(
            reminders = reminders,
            availableTags = availableTags,
            onUpdate = onUpdate,
            onDelete = onDelete,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(padding),
        )
    }
}

@Composable
fun ReminderList(
    reminders: ImmutableList<Reminder>,
    availableTags: ImmutableMap<String, ImmutableList<String>>,
    onUpdate: (Reminder) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(reminders, key = { it.id }) { reminder ->
                // FIXME: move to editor screen
                Column(modifier = Modifier.padding(vertical = 24.dp)) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = reminder.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1F),
                        )

                        IconButton(onClick = { onDelete(reminder.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                        }
                    }

                    Text(
                        text = "Wochentage",
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
                                checked = dow in reminder.weekdays,
                                onCheckedChange = {
                                    onUpdate(
                                        reminder.copy(
                                            weekdays = reminder.weekdays.toggle(
                                                dow
                                            )
                                        )
                                    )
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

                    Spacer(modifier = Modifier.height(8.dp))


                    Text(
                        text = "Erste Erinnerung",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    // FIXME
                    OutlinedTextField(
                        value = LocalTime.now().toString(),
                        onValueChange = {},
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wiederholungen am Tag",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {

                        // FIXME
                        DurationTextField(
                            value = null,
                            onValueChange = {},
                            required = false, // FIXME: if other
                            modifier = Modifier.weight(1F),
                            min = 15.minutes,
                            label = {
                                Text(text = "Interval")
                            }
                        )

                        OutlinedTextField(
                            // FIXME: > remindedAt
                            value = LocalTime.now().plusHours(8L).coerceAtMost(LocalTime.MAX)
                                .toString(),
                            onValueChange = {},
                            modifier = Modifier.weight(1F),
                            label = {
                                Text(text = "Bis")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    Text(
                        text = "Filter",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    RoutineFilterRow(
                        availableTags = availableTags,
                        selectedTags = reminder.filter.selectedTags.toImmutableList(),
                        isOnlyFavorites = reminder.filter.onlyFavorites,
                        durationFilter = reminder.filter.durationFilter,
                        onDurationFilterChange = {
                            onUpdate(
                                reminder.copy(
                                    filter = reminder.filter.copy(
                                        durationFilter = it
                                    )
                                )
                            )
                        },
                        onOnlyFavoritesChange = {
                            onUpdate(
                                reminder.copy(
                                    filter = reminder.filter.copy(
                                        onlyFavorites = it
                                    )
                                )
                            )
                        },
                        onSelectedTagsChange = {
                            onUpdate(
                                reminder.copy(
                                    filter = reminder.filter.copy(
                                        selectedTags = it
                                    )
                                )
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    )
                }

                HorizontalDivider()
            }
        }
    }
}

private fun Set<DayOfWeek>.toggle(dayOfWeek: DayOfWeek): Set<DayOfWeek> = if (dayOfWeek in this) {
    this - dayOfWeek
} else {
    this + dayOfWeek
}