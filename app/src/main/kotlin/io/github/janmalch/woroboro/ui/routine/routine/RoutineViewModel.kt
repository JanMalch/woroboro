package io.github.janmalch.woroboro.ui.routine.routine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.models.Routine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val uiState = savedStateHandle.getStateFlow<String?>(ROUTINE_SCREEN_ARG_ID, null)
        .filterNotNull()
        .map { UUID.fromString(it) }
        .flatMapLatest(routineRepository::findOne)
        .map { if (it == null) RoutineUiState.Failure else RoutineUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutineUiState.Loading,
        )


    fun toggleFavorite(routine: Routine) {
        val isFavorite = routine.isFavorite
        viewModelScope.launch {
            // FIXME
        }
    }

}

sealed interface RoutineUiState {
    data object Loading : RoutineUiState
    data class Success(val routine: Routine) : RoutineUiState
    data object Failure : RoutineUiState
}
