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
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.EditedExercise
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
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
class ExerciseEditorViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val exerciseId = MutableStateFlow(ExerciseEditorArgs(savedStateHandle).exerciseId)
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("ExerciseEditorViewModel", "Error while saving or removing exercise.", exception)
    }

    var isLoading by mutableStateOf(false)
        private set

    val exerciseToEdit = exerciseId.flatMapLatest { id ->
        id?.let { exerciseRepository.resolve(it) } ?: flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val availableTags = tagRepository.findAllGrouped().map { allTags ->
        allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = persistentMapOf(),
    )

    fun save(exercise: EditedExercise) {
        viewModelScope.launch(exceptionHandler) {
            try {
                exerciseId.value = if (exerciseId.value == null) {
                    exerciseRepository.insert(exercise)
                } else {
                    exerciseRepository.update(exercise)
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun delete(id: UUID) {
        viewModelScope.launch(exceptionHandler) {
            try {
                exerciseRepository.delete(id)
                exerciseId.value = null
            } finally {
                isLoading = false
            }
        }
    }
}