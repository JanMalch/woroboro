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
