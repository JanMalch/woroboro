package io.github.janmalch.woroboro.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ReminderRepository
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.Reminder
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    tagRepository: TagRepository,
    private val reminderRepository: ReminderRepository,
) : ViewModel() {

    val reminders = reminderRepository.findAll()
        .map(List<Reminder>::toImmutableList)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf(),
        )

    val availableTags = tagRepository.findAllGrouped().map { allTags ->
        allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = persistentMapOf(),
    )

    fun insert(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.insert(reminder)
        }
    }


    fun update(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.update(reminder)
        }
    }

    fun delete(reminderId: UUID) {
        viewModelScope.launch {
            reminderRepository.delete(reminderId)
        }
    }
}