package io.github.janmalch.woroboro.ui.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.ExerciseRepository
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.EditedExercise
import io.github.janmalch.woroboro.models.Exercise
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.ui.findAvailableTags
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SELECTED_TAGS_SSH_KEY = "selected_tags"
private const val ONLY_FAVORITES_SSH_KEY = "only_favorites"
private const val TEXT_QUERY_SSH_KEY = "text_query"

@HiltViewModel
class ExerciseListViewModel
@Inject
constructor(
    private val exerciseRepository: ExerciseRepository,
    private val tagRepository: TagRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _selectedTagLabels =
        savedStateHandle.getStateFlow(SELECTED_TAGS_SSH_KEY, emptyList<String>())

    val isOnlyFavorites = savedStateHandle.getStateFlow(ONLY_FAVORITES_SSH_KEY, false)

    val textQuery = savedStateHandle.getStateFlow(TEXT_QUERY_SSH_KEY, "")

    val selectedTags =
        _selectedTagLabels
            .flatMapLatest { tagRepository.resolveAll(it).map(List<Tag>::toImmutableList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = persistentListOf(),
            )

    val exercises =
        combine(_selectedTagLabels, isOnlyFavorites, textQuery, ::Triple)
            .flatMapLatest { (selectedTags, isOnlyFavorites, textQuery) ->
                exerciseRepository
                    .findAll(
                        selectedTags,
                        onlyFavorites = isOnlyFavorites,
                        textQuery = textQuery,
                    )
                    .map(List<Exercise>::toImmutableList)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = persistentListOf(),
            )

    val availableTags =
        tagRepository
            .findAvailableTags()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = persistentMapOf(),
            )

    fun toggleFavorite(exercise: Exercise) {
        val isFavorite = exercise.isFavorite
        viewModelScope.launch {
            exerciseRepository.update(
                EditedExercise(
                    exercise = exercise.copy(isFavorite = !isFavorite),
                    addedMedia = emptySet(),
                )
            )
        }
    }

    fun changeSelectedTags(tags: List<Tag>) {
        savedStateHandle[SELECTED_TAGS_SSH_KEY] = tags.map(Tag::label)
    }

    fun setOnlyFavorites(onlyFavorites: Boolean) {
        savedStateHandle[ONLY_FAVORITES_SSH_KEY] = onlyFavorites
    }

    fun setTextQuery(query: String) {
        savedStateHandle[TEXT_QUERY_SSH_KEY] = query
    }
}
