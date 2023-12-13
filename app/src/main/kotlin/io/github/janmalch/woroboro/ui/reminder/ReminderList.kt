package io.github.janmalch.woroboro.ui.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.github.janmalch.woroboro.models.Reminder
import kotlinx.collections.immutable.ImmutableList
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

@Composable
fun ReminderListScreen(
    reminders: ImmutableList<Reminder>,
    onNewReminder: () -> Unit,
    onGoToReminder: (UUID) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Erinnerungen") })
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
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            items(reminders, key = { it.id }) { reminder ->
                ListItem(
                    headlineContent = { Text(reminder.name) },
                    supportingContent = {
                        Text(text = buildString {
                            val weekdays = reminder.weekdays.joinToString {
                                it.getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.getDefault()
                                )
                            }
                            append(weekdays)
                            append(" · ")
                            // TODO: i18n
                            append(
                                reminder.remindAt.format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                                )
                            )
                        })

                    },
                    modifier = Modifier.clickable { onGoToReminder(reminder.id) }
                )

                HorizontalDivider()
            }
        }
    }
}
