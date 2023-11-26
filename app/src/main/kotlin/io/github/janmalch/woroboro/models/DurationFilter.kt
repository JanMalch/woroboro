package io.github.janmalch.woroboro.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class DurationFilter(val duration: Duration) {
    VeryShort(5.minutes),
    Short(15.minutes),
    Medium(30.minutes),
    Any(Duration.INFINITE)
}
