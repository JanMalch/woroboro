package io.github.janmalch.woroboro.ui.routine.routine

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

/**
 * @author
 *   https://github.com/nihk/stopwatch-fun/blob/6c8e8c70d6215931fae97f637ea9670cca00c5f5/app/src/main/java/com/example/stopwatch_fun/Stopwatch.kt
 */
interface Stopwatch {
    val state: StateFlow<State>
    val time: Flow<Duration>

    fun start()

    fun pause()

    fun reset()

    enum class State {
        Paused,
        Reset,
        Running,
    }

    companion object {
        operator fun invoke(): Stopwatch = Default()
    }

    private class Default(
        private val tick: Long = 10L,
        private val currentTime: () -> Long = { System.currentTimeMillis() }
    ) : Stopwatch {
        private var elapsedTime: Long = 0L
        private val _state = MutableStateFlow(State.Reset)

        override val state = _state.asStateFlow()
        override val time: Flow<Duration> =
            _state
                .flatMapLatest { action ->
                    when (action) {
                        State.Running -> {
                            val time = flow {
                                val initial = currentTime() - elapsedTime
                                while (currentCoroutineContext().isActive) {
                                    elapsedTime = currentTime() - initial
                                    emit(elapsedTime)
                                    delay(tick)
                                }
                            }
                            time.conflate()
                        }
                        State.Paused -> flowOf(elapsedTime)
                        State.Reset -> {
                            elapsedTime = 0L
                            flowOf(elapsedTime)
                        }
                    }
                }
                .map { it.milliseconds }

        override fun start() {
            _state.value = State.Running
        }

        override fun pause() {
            _state.value = State.Paused
        }

        override fun reset() {
            _state.value = State.Reset
        }
    }
}

fun Stopwatch.toggle() {
    if (state.value == Stopwatch.State.Running) {
        pause()
    } else {
        start()
    }
}
