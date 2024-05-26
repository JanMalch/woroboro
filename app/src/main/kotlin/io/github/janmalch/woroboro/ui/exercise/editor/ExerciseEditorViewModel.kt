package io.github.janmalch.woroboro.ui.exercise.editor

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
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.EditedExercise
import io.github.janmalch.woroboro.ui.Outcome
import io.github.janmalch.woroboro.ui.findAvailableTags
import java.util.UUID
import javax.inject.Inject
import kotlinx.collections.immutable.persistentMapOf
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
class ExerciseEditorViewModel
@Inject
constructor(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val exerciseId = MutableStateFlow(ExerciseEditorArgs(savedStateHandle).exerciseId)
    private val saveExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ExerciseEditorViewModel", "Error while saving exercise.", exception)
        viewModelScope.launch {
            isLoading = false
            _onSaveFinished.send(Outcome.Failure)
        }
    }
    private val deleteExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ExerciseEditorViewModel", "Error while removing exercise.", exception)
        viewModelScope.launch { _onDeleteFinished.send(Outcome.Failure) }
    }
    private val addToRoutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ExerciseEditorViewModel", "Error while adding exercise to routine.", exception)
        viewModelScope.launch { _onAddToRoutineFinished.send(Outcome.Failure) }
    }

    var isLoading by mutableStateOf(false)
        private set

    val exerciseToEdit =
        exerciseId
            .flatMapLatest { id ->
                if (id == null) flowOf(null) else exerciseRepository.resolve(id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    val availableTags =
        tagRepository
            .findAvailableTags()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = persistentMapOf(),
            )

    val allRoutines =
        routineRepository
            .findAll(
                tags = emptyList(),
                onlyFavorites = false,
                durationFilter = DurationFilter.Any,
                textQuery = "",
            )
            .map { list -> list.toImmutableList() }

    private val _onSaveFinished = Channel<Outcome>()
    val onSaveFinished = _onSaveFinished.receiveAsFlow()

    private val _onDeleteFinished = Channel<Outcome>()
    val onDeleteFinished = _onDeleteFinished.receiveAsFlow()

    private val _onAddToRoutineFinished = Channel<Outcome>()
    val onAddToRoutineFinished = _onAddToRoutineFinished.receiveAsFlow()

    fun save(exercise: EditedExercise) {
        // don't reset loading, because we navigate away on success
        isLoading = true
        viewModelScope.launch(saveExceptionHandler) {
            exerciseId.value =
                if (exerciseId.value == null) {
                    exerciseRepository.insert(exercise)
                } else {
                    exerciseRepository.update(exercise)
                }
            _onSaveFinished.send(Outcome.Success)
        }
    }

    fun delete(id: UUID) {
        viewModelScope.launch(deleteExceptionHandler) {
            exerciseRepository.delete(id)
            _onDeleteFinished.send(Outcome.Success)
        }
    }

    fun addToRoutine(exerciseId: UUID, routineId: UUID) {
        viewModelScope.launch(addToRoutineExceptionHandler) {
            routineRepository.appendExerciseToRoutine(
                exerciseId = exerciseId,
                routineId = routineId,
            )
            _onAddToRoutineFinished.send(Outcome.Success)
        }
    }
}
