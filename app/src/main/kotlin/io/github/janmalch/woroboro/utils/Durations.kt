package io.github.janmalch.woroboro.utils

import kotlin.time.Duration
import kotlin.time.DurationUnit

fun findWholeUnit(duration: Duration?, default: DurationUnit = DurationUnit.SECONDS): DurationUnit {
    if (duration == null || duration == Duration.ZERO) return default
    return duration.toComponents { _, minutes, seconds, _ ->
        when {
            seconds != 0 -> DurationUnit.SECONDS
            minutes != 0 -> DurationUnit.MINUTES
            else -> DurationUnit.HOURS
        }
    }
}


fun formatForTimer(duration: Duration): String = duration.toComponents { minutes, seconds, _ ->
    // TODO: i18n?
    "${minutes.toString(10).padStart(2, '0')}:${seconds.toString(10).padStart(2, '0')}"
}
