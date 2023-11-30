package io.github.janmalch.woroboro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

enum class Outcome {
    Success,
    Failure
}

@Composable
fun <T> CollectAsEvents(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: suspend (T) -> Unit
) = LaunchedEffect(flow, lifecycleOwner.lifecycle) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        withContext(Dispatchers.Main.immediate) {
            flow.collect(onEvent)
        }
    }
}
