package io.github.janmalch.woroboro.ui.routine

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.LaunchDataService
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.DurationFilter
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.models.Tag
import io.github.janmalch.woroboro.utils.Quad
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
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
    launchDataService: LaunchDataService,
) : ViewModel() {

    private val _routineFilter = launchDataService.consumeRoutineFilter()

    private val _selectedTagLabels =
        savedStateHandle.getStateFlow(SELECTED_TAGS_SSH_KEY,
            _routineFilter?.selectedTags?.map { it.label } ?: emptyList()
        )

    val textQuery =
        savedStateHandle.getStateFlow(TEXT_QUERY_SSH_KEY, "")

    val isOnlyFavorites =
        savedStateHandle.getStateFlow(
            ONLY_FAVORITES_SSH_KEY,
            _routineFilter?.onlyFavorites ?: false
        )

    val durationFilter =
        savedStateHandle.getStateFlow(
            DURATION_FILTER_SSH_KEY,
            _routineFilter?.durationFilter ?: DurationFilter.Any
        )

    // TODO: combine tag flows?
    private val selectedTags = _selectedTagLabels.flatMapLatest {
        tagRepository.resolveAll(it).map(List<Tag>::toImmutableList)
    }

    private val availableTags = tagRepository.findAllGrouped().map { allTags ->
        allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }

    private val routines = combine(
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
    }

    val uiState = combine(routines, selectedTags, availableTags, RoutineListUiState::Success)
        .widen<RoutineListUiState, RoutineListUiState.Success>()
        .catch {
            Log.e("RoutineListViewModel", "Error while building UI state.", it)
            emit(RoutineListUiState.Failure)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutineListUiState.Loading,
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

sealed interface RoutineListUiState {
    data class Success(
        val routines: ImmutableList<Routine>,
        val selectedTags: ImmutableList<Tag>,
        val availableTags: ImmutableMap<String, ImmutableList<String>>,
    ) : RoutineListUiState

    data object Loading : RoutineListUiState
    data object Failure : RoutineListUiState
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <A, B : A> Flow<B>.widen(): Flow<A> = this
