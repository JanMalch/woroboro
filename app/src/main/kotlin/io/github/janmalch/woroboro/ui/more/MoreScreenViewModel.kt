package io.github.janmalch.woroboro.ui.more

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.ui.Outcome
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreScreenViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
) : ViewModel() {

    private val _onClearLastRunsFinished = Channel<Outcome>()
    val onClearLastRunsFinished = _onClearLastRunsFinished.receiveAsFlow()

    private val clearLastRunsExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("MoreScreenViewModel", "Error while clearing last runs.", exception)
        viewModelScope.launch {
            _onClearLastRunsFinished.send(Outcome.Failure)
        }
    }

    fun clearLastRuns() {
        viewModelScope.launch(clearLastRunsExceptionHandler) {
            routineRepository.clearLastRuns()
            _onClearLastRunsFinished.send(Outcome.Success)
        }
    }

}