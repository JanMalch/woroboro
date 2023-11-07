package io.github.janmalch.woroboro.ui.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ExerciseRepository
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Tag
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SELECTED_TAGS_SSH_KEY = "selected_tags"

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val tagRepository: TagRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _selectedTagLabels =
        savedStateHandle.getStateFlow(SELECTED_TAGS_SSH_KEY, emptyList<String>())

    val selectedTags = _selectedTagLabels.flatMapLatest {
        tagRepository.resolveAll(it).map(List<Tag>::toImmutableList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = persistentListOf(),
    )

    val exercises = _selectedTagLabels.flatMapLatest { selectedTags ->
        exerciseRepository.findByTags(selectedTags).map(List<Exercise>::toImmutableList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = persistentListOf(),
    )

    val availableTags = flow {
        val allTags = tagRepository.findAllGrouped()
        val immutable = allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
        emit(immutable)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = persistentMapOf(),
    )

    fun toggleFavorite(exercise: Exercise) {
        val isFavorite = exercise.isFavorite
        viewModelScope.launch {
            exerciseRepository.update(exercise.copy(isFavorite = !isFavorite))
        }
    }

    fun changeSelectedTags(tags: List<Tag>) {
        savedStateHandle[SELECTED_TAGS_SSH_KEY] = tags.map(Tag::label)
    }
}