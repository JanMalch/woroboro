package io.github.janmalch.woroboro.ui.routine.editor

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ExerciseRepository
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.ui.Outcome
import java.util.UUID
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

@HiltViewModel
class RoutineEditorViewModel
@Inject
constructor(
    private val routineRepository: RoutineRepository,
    exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val routineId = MutableStateFlow(RoutineEditorArgs(savedStateHandle).routineId)
    private val saveExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("RoutineEditorViewModel", "Error while saving routine.", exception)
        viewModelScope.launch {
            isLoading = false
            _onSaveFinished.send(Outcome.Failure)
        }
    }
    private val deleteExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("RoutineEditorViewModel", "Error while removing routine.", exception)
        viewModelScope.launch { _onDeleteFinished.send(Outcome.Failure) }
    }

    var isLoading by mutableStateOf(false)
        private set

    val routineToEdit =
        routineId
            .flatMapLatest { id -> if (id == null) flowOf(null) else routineRepository.findOne(id) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = null,
            )

    val allExercises =
        exerciseRepository
            .findAll(tags = emptyList(), onlyFavorites = false, textQuery = "")
            .map { list -> list.toImmutableList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = persistentListOf(),
            )

    private val _onSaveFinished = Channel<Outcome>()
    val onSaveFinished = _onSaveFinished.receiveAsFlow()

    private val _onDeleteFinished = Channel<Outcome>()
    val onDeleteFinished = _onDeleteFinished.receiveAsFlow()

    fun save(routine: FullRoutine) {
        // don't reset loading, because we navigate away on success
        isLoading = true
        viewModelScope.launch(saveExceptionHandler) {
            if (routineId.value == null) {
                routineRepository.insert(routine)
            } else {
                routineRepository.update(routine)
            }
            _onSaveFinished.send(Outcome.Success)
        }
    }

    fun delete(id: UUID) {
        viewModelScope.launch(deleteExceptionHandler) {
            routineRepository.delete(id)
            routineId.value = null
            _onDeleteFinished.send(Outcome.Success)
        }
    }
}
