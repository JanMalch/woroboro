package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.TagDao
import io.github.janmalch.woroboro.models.Tag
import javax.inject.Inject

interface TagRepository {
    suspend fun upsert(tag: Tag)
    suspend fun delete(label: String)
    suspend fun findAllTags(): Map<String, List<String>>
}

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override suspend fun upsert(tag: Tag) {
        tagDao.upsert(tag.asEntity())
    }

    override suspend fun delete(label: String) {
        tagDao.delete(label)
    }

    override suspend fun findAllTags(): Map<String, List<String>> {
        return tagDao.findAllTags()
            .groupBy(keySelector = { it.type }, valueTransform = { it.label })
            .mapValues { it.value.sorted() }
    }

}
