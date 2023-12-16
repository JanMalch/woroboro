package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlin.time.Duration

@Composable
@ReadOnlyComposable
fun formatDuration(duration: Duration, zero: String = seconds(0)): String {
    if (duration == Duration.ZERO) return zero
    return duration.toComponents { hours, minutes, seconds, _ ->
        when {
            seconds != 0 -> seconds(seconds)
            minutes != 0 -> minutes(minutes)
            else -> hours(hours)
        }
    }
}

@Composable
@ReadOnlyComposable
fun seconds(value: Long): String {
    return "${value}s" // TODO: translate
}

@Composable
@ReadOnlyComposable
fun seconds(value: Int): String {
    return "${value}s" // TODO: translate
}

@Composable
@ReadOnlyComposable
fun minutes(value: Long): String {
    return "${value}m" // TODO: translate
}

@Composable
@ReadOnlyComposable
fun minutes(value: Int): String {
    return "${value}m" // TODO: translate
}

@Composable
@ReadOnlyComposable
fun hours(value: Long): String {
    return "${value}h" // TODO: translate
}

@Composable
@ReadOnlyComposable
fun hours(value: Int): String {
    return "${value}h" // TODO: translate
}
