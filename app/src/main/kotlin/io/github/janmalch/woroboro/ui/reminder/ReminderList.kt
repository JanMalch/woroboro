package io.github.janmalch.woroboro.ui.reminder

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.R
import io.github.janmalch.woroboro.models.Reminder
import kotlinx.collections.immutable.ImmutableList
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

@Composable
fun ReminderListScreen(
    reminders: ImmutableList<Reminder>,
    onNewReminder: () -> Unit,
    onGoToReminder: (UUID) -> Unit,
    onToggleReminderActive: (reminderId: UUID, shouldActivate: Boolean) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(id = R.string.reminders)) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewReminder,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        },
    ) { padding ->
        ReminderList(
            reminders = reminders,
            onGoToReminder = onGoToReminder,
            onToggleReminderActive = onToggleReminderActive,
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
    onGoToReminder: (UUID) -> Unit,
    onToggleReminderActive: (reminderId: UUID, shouldActivate: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormat = stringResource(id = R.string.time_format)
    val dtf = remember(timeFormat) { DateTimeFormatter.ofPattern(timeFormat) }
    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            // So reminder IDs are not stable (update generates a new ID) so we can't use that key,
            // but indices can't change (currently), so it is a suitable key.
            itemsIndexed(reminders, key = { idx, _ -> idx }) { _, reminder ->
                ListItem(
                    headlineContent = { Text(reminder.name) },
                    supportingContent = {
                        Text(text = buildString {
                            val weekdays = reminder.weekdays.joinRangesToString()
                            append(weekdays)
                            append(" · ")
                            append(
                                reminder.remindAt.format(dtf)
                            )
                        })

                    },
                    trailingContent = {
                        Switch(
                            checked = reminder.isActive,
                            onCheckedChange = { onToggleReminderActive(reminder.id, it) },
                            thumbContent = {
                                if (reminder.isActive) {
                                    Icon(
                                        Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.NotificationsOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        )
                    },
                    modifier = Modifier.clickable { onGoToReminder(reminder.id) }
                )

                HorizontalDivider()
            }
        }
    }
}

@VisibleForTesting
fun Set<DayOfWeek>.joinRangesToString(locale: Locale = Locale.getDefault()): String {
    if (isEmpty()) return ""
    if (size == 1) return first().getDisplayName(TextStyle.SHORT, locale)

    val sorted = sortedBy { it.value }
    val ranges = mutableListOf(mutableListOf(sorted.first()))
    for (next in sorted.drop(1)) {
        val adjacent = next.value == (ranges.last().last().value + 1)
        if (adjacent) {
            ranges.last().add(next)
        } else {
            ranges.add(mutableListOf(next))
        }
    }
    if (ranges.last().size == 1 && sorted.last() != ranges.last().last()) {
        ranges.last().add(sorted.last())
    }
    return ranges.joinToString {
        if (it.size == 1) {
            it.first().getDisplayName(TextStyle.SHORT, locale)
        } else {
            it.first().getDisplayName(TextStyle.SHORT, locale) + " – " + it.last()
                .getDisplayName(TextStyle.SHORT, locale)
        }
    }
}
