package io.github.janmalch.woroboro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel() {

    // FIXME: remove all this until needed
    val state = flowOf(MainUiState.Success)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainUiState.Loading,
        )

}

sealed interface MainUiState {
    data object Loading : MainUiState
    // make it a data class with important data that must be ready for app starts
    data object Success : MainUiState
}
