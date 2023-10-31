package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.janmalch.woroboro.data.model.ExerciseWithTagsEntity
import io.github.janmalch.woroboro.data.model.TagEntity

@Dao
interface TagDao {

    @Upsert
    suspend fun upsert(tagEntity: TagEntity)

    @Query("DELETE FROM tag WHERE label = :label")
    suspend fun delete(label: String)

}
