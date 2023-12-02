package io.github.janmalch.woroboro.ui.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.utils.Quad
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SELECTED_TAGS_SSH_KEY = "selected_tags"
private const val ONLY_FAVORITES_SSH_KEY = "only_favorites"
private const val DURATION_FILTER_SSH_KEY = "duration_filter"
private const val TEXT_QUERY_SSH_KEY = "text_query"

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val tagRepository: TagRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _selectedTagLabels =
        savedStateHandle.getStateFlow(SELECTED_TAGS_SSH_KEY, emptyList<String>())

    val textQuery =
        savedStateHandle.getStateFlow(TEXT_QUERY_SSH_KEY, "")

    val isOnlyFavorites =
        savedStateHandle.getStateFlow(ONLY_FAVORITES_SSH_KEY, false)

    val durationFilter =
        savedStateHandle.getStateFlow(DURATION_FILTER_SSH_KEY, DurationFilter.Any)

    val selectedTags = _selectedTagLabels.flatMapLatest {
        tagRepository.resolveAll(it).map(List<Tag>::toImmutableList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = persistentListOf(),
    )

    val routines = combine(
        _selectedTagLabels,
        isOnlyFavorites,
        durationFilter,
        textQuery.map(String::trim),
        ::Quad
    ).flatMapLatest { (selectedTags, isOnlyFavorites, durationFilter, textQuery) ->
        routineRepository.findAll(
            selectedTags,
            onlyFavorites = isOnlyFavorites,
            durationFilter = durationFilter,
            textQuery = textQuery,
        )
            .map(List<Routine>::toImmutableList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = persistentListOf(),
    )

    val availableTags = tagRepository.findAllGrouped().map { allTags ->
        allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = persistentMapOf(),
    )

    fun toggleFavorite(routine: Routine) {
        val isFavorite = routine.isFavorite
        val update = routine.copy(isFavorite = !isFavorite)
        viewModelScope.launch {
            routineRepository.update(update)
        }
    }

    fun changeSelectedTags(tags: List<Tag>) {
        savedStateHandle[SELECTED_TAGS_SSH_KEY] = tags.map(Tag::label)
    }

    fun setOnlyFavorites(onlyFavorites: Boolean) {
        savedStateHandle[ONLY_FAVORITES_SSH_KEY] = onlyFavorites
    }

    fun setDurationFilter(durationFilter: DurationFilter) {
        savedStateHandle[DURATION_FILTER_SSH_KEY] = durationFilter
    }

    fun setTextQuery(query: String) {
        savedStateHandle[TEXT_QUERY_SSH_KEY] = query
    }
}
