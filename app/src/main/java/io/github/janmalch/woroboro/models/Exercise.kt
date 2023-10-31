package io.github.janmalch.woroboro.models

import java.util.UUID

data class Exercise(
    val id: UUID,
    val name: String,
    val description: String,
)
