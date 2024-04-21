package io.github.janmalch.woroboro.ui.reminder

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ReminderRepository
import io.github.janmalch.woroboro.business.activate
import io.github.janmalch.woroboro.business.deactivate
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.ui.Outcome
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
) : ViewModel() {


    private val _onToggleReminder = Channel<Outcome>()
    val onToggleReminder = _onToggleReminder.receiveAsFlow()

    private val toggleReminderExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ReminderListViewModel", "Error while toggling reminder active state.", exception)
        viewModelScope.launch {
            _onToggleReminder.send(Outcome.Failure)
        }
    }

    val reminders = reminderRepository.findAll()
        .map(List<Reminder>::toImmutableList)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf(),
        )

    fun toggleReminderActive(reminderId: UUID, shouldActivate: Boolean) {
        viewModelScope.launch(toggleReminderExceptionHandler) {
            if (shouldActivate) {
                reminderRepository.activate(reminderId)
            } else {
                reminderRepository.deactivate(reminderId)
            }
        }
    }
}