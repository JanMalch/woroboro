package io.github.janmalch.woroboro.business

import io.github.janmalch.woroboro.data.dao.TagDao
import io.github.janmalch.woroboro.data.model.TagEntity
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface TagRepository {
    suspend fun upsert(tag: Tag)
    suspend fun delete(label: String)
    suspend fun findAllGrouped(): Map<String, List<String>>
    fun resolveAll(labels: List<String>): Flow<List<Tag>>
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

    override suspend fun findAllGrouped(): Map<String, List<String>> {
        return tagDao.findAll()
            .first()
            .groupBy(keySelector = { it.type }, valueTransform = { it.label })
            .mapValues { it.value.sorted() }
    }

    override fun resolveAll(labels: List<String>): Flow<List<Tag>> {
        return tagDao.resolve(labels).map { list -> list.map(TagEntity::asModel) }
    }

}
