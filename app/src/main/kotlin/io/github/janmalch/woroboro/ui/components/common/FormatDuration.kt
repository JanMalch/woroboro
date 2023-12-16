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
        when {
            seconds != 0 -> seconds(seconds)
            minutes != 0 -> minutes(minutes)
            else -> hours(hours)
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
