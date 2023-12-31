package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import io.github.janmalch.woroboro.R
import kotlin.time.Duration

@Composable
@ReadOnlyComposable
fun formatDuration(duration: Duration, zero: String = seconds(0)): String {
    if (duration == Duration.ZERO) return zero
    return duration.toComponents { hours, minutes, seconds, _ ->
        buildString {
            if (hours > 0) {
                append(hours(hours))
            }
            if (minutes > 0) {
                append(minutes(minutes))
            }
            if (seconds > 0 || isEmpty()) {
                append(seconds(seconds))
            }
        }
    }
}

@Composable
@ReadOnlyComposable
fun seconds(value: Long): String = stringResource(id = R.string.duration_in_seconds, value)

@Composable
@ReadOnlyComposable
fun seconds(value: Int): String = stringResource(id = R.string.duration_in_seconds, value)

@Composable
@ReadOnlyComposable
fun minutes(value: Long): String = stringResource(id = R.string.duration_in_minutes, value)

@Composable
@ReadOnlyComposable
fun minutes(value: Int): String = stringResource(id = R.string.duration_in_minutes, value)

@Composable
@ReadOnlyComposable
fun hours(value: Long): String = stringResource(id = R.string.duration_in_hours, value)

@Composable
@ReadOnlyComposable
fun hours(value: Int): String = stringResource(id = R.string.duration_in_hours, value)
