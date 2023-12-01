package io.github.janmalch.woroboro.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class DurationFilter(val range: OpenEndRange<Duration>) {
    VeryShort(Duration.ZERO..<5.minutes),
    Short(5.minutes..<15.minutes),
    Medium(15.minutes..<30.minutes),
    Long(30.minutes..<Duration.INFINITE),
    Any(Duration.ZERO..<Duration.INFINITE),
}
