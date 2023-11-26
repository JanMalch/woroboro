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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoutineEditorViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val routineId = MutableStateFlow(RoutineEditorArgs(savedStateHandle).routineId)
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("RoutineEditorViewModel", "Error while saving or removing routine.", exception)
    }
    var isLoading by mutableStateOf(false)
        private set

    val routineToEdit = routineId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else routineRepository.findOne(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null,
    )

    val allExercises = exerciseRepository
        .findByTags(tags = emptyList(), onlyFavorites = false)
        .map { list -> list.toImmutableList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = persistentListOf(),
        )

    fun save(routine: FullRoutine) {
        isLoading = true
        viewModelScope.launch(exceptionHandler) {
            try {
                routineId.value = if (routineId.value == null) {
                    routineRepository.insert(routine)
                } else {
                    routineRepository.update(routine)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun delete(id: UUID) {
        isLoading = true
        viewModelScope.launch(exceptionHandler) {
            try {
                routineRepository.delete(id)
                routineId.value = null
            } finally {
                isLoading = false
            }
        }
    }
}