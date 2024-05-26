package io.github.janmalch.woroboro.ui.exercise.tageditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.woroboro.business.TagRepository
import io.github.janmalch.woroboro.models.Tag
import javax.inject.Inject
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TagEditorViewModel
@Inject
constructor(
    private val tagRepository: TagRepository,
) : ViewModel() {

    val groupedTags =
        tagRepository
            .findAllGrouped()
            .map { allTags ->
                allTags
                    .mapValues {
                        it.value
                            .map { label -> Tag(type = it.key, label = label) }
                            .toImmutableList()
                    }
                    .toImmutableMap()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = persistentMapOf(),
            )

    fun addTag(tag: Tag) {
        viewModelScope.launch { tagRepository.insert(tag) }
    }

    fun updateTag(tag: Tag, oldLabel: String) {
        viewModelScope.launch { tagRepository.update(tag, oldLabel) }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch { tagRepository.delete(tag.label) }
    }

    fun renameType(fromTo: Pair<String, String>) {
        viewModelScope.launch { tagRepository.renameType(fromTo.first, fromTo.second) }
    }
}
