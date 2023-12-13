package io.github.janmalch.woroboro.ui.reminder.editor

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ReminderRepository
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.ui.Outcome
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReminderEditorViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {


    private val reminderId = MutableStateFlow(RoutineEditorArgs(savedStateHandle).reminderId)

    private val _onSaveFinished = Channel<Outcome>()
    val onSaveFinished = _onSaveFinished.receiveAsFlow()

    private val _onDeleteFinished = Channel<Outcome>()
    val onDeleteFinished = _onDeleteFinished.receiveAsFlow()

    val reminderToEdit = reminderId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else reminderRepository.findOne(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private val saveExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ReminderEditorViewModel", "Error while saving reminder.", exception)
        viewModelScope.launch {
            _onSaveFinished.send(Outcome.Failure)
        }
    }
    private val deleteExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ReminderEditorViewModel", "Error while removing reminder.", exception)
        viewModelScope.launch {
            _onDeleteFinished.send(Outcome.Failure)
        }
    }

    val availableTags = tagRepository.findAllGrouped().map { allTags ->
        allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = persistentMapOf(),
    )

    fun save(reminder: Reminder) {
        viewModelScope.launch(saveExceptionHandler) {
            if (reminderId.value == null) {
                reminderRepository.insert(reminder)
            } else {
                reminderRepository.update(reminder)
            }
            _onSaveFinished.send(Outcome.Success)
        }
    }

    fun delete(reminderId: UUID) {
        viewModelScope.launch(deleteExceptionHandler) {
            reminderRepository.delete(reminderId)
            _onDeleteFinished.send(Outcome.Success)
        }
    }
}