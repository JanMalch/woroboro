package io.github.janmalch.woroboro.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ReminderRepository
import io.github.janmalch.woroboro.models.Reminder
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    reminderRepository: ReminderRepository,
) : ViewModel() {

    val reminders = reminderRepository.findAll()
        .map(List<Reminder>::toImmutableList)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf(),
        )
}