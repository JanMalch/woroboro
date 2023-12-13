package io.github.janmalch.woroboro.ui

import io.github.janmalch.woroboro.business.TagRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.map

fun TagRepository.findAvailableTags() = findAllGrouped().map { allTags ->
    allTags.mapValues { it.value.toImmutableList() }.toImmutableMap()
}
