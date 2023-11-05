package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.janmalch.woroboro.data.model.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Upsert
    suspend fun upsert(tagEntity: TagEntity)

    @Query("DELETE FROM tag WHERE label = :label")
    suspend fun delete(label: String)

    @Query("SELECT * FROM tag ORDER BY type ASC")
    fun findAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE label in (:labels) ORDER BY type ASC")
    fun resolve(labels: List<String>): Flow<List<TagEntity>>

}
