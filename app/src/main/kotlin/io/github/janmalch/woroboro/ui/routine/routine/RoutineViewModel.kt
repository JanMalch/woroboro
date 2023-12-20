package io.github.janmalch.woroboro.ui.routine.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.models.FullRoutine
import io.github.janmalch.woroboro.models.RoutineStep
import io.github.janmalch.woroboro.models.asRoutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _finishedSteps = MutableStateFlow(listOf<UUID>())

    val uiState = savedStateHandle.getStateFlow<String?>(ROUTINE_SCREEN_ARG_ID, null)
        .filterNotNull()
        .map { UUID.fromString(it) }
        .flatMapLatest(routineRepository::findOne)
        .combine(_finishedSteps) { fullRoutine, finishedSteps ->
            if (fullRoutine == null) RoutineUiState.Failure else {
                val (finished, unfinished) = fullRoutine.steps.partition { it.id in finishedSteps }
                RoutineUiState.Success(
                    routine = fullRoutine,
                    // retain order
                    finished = finished.sortedBy { finishedSteps.indexOf(it.id) },
                    unfinished = unfinished,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed with immediate timeout to refresh after save in editor.
            started = SharingStarted.WhileSubscribed(),
            initialValue = RoutineUiState.Loading,
        )

    fun finishStep(step: RoutineStep) {
        _finishedSteps.value = _finishedSteps.value + step.id
    }

    fun undoStep(step: RoutineStep) {
        _finishedSteps.value = _finishedSteps.value - step.id
    }

    fun saveAsLastRun(routine: FullRoutine, duration: Duration) {
        if (duration <= 1.seconds) return
        val ended = LocalDateTime.now()
        val update = routine.asRoutine().copy(lastRunDuration = duration, lastRunEnded = ended)
        viewModelScope.launch {
            routineRepository.update(update)
        }
    }

}

sealed interface RoutineUiState {
    data object Loading : RoutineUiState
    data class Success(
        val routine: FullRoutine,
        val finished: List<RoutineStep>,
        val unfinished: List<RoutineStep>,
    ) : RoutineUiState {
        val finishedExercises = finished.filterIsInstance<RoutineStep.ExerciseStep>()
        val unfinishedExercises = unfinished.filterIsInstance<RoutineStep.ExerciseStep>()
    }

    data object Failure : RoutineUiState
}
