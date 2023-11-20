package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.github.janmalch.woroboro.data.model.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TagDao {

    @Insert
    abstract suspend fun insert(tagEntity: TagEntity)

    @Query(
        """
        UPDATE exercise_tag_cross_ref
        SET tag_label = :newLabel
        WHERE tag_label = :oldLabel
    """
    )
    protected abstract suspend fun updateRefs(oldLabel: String, newLabel: String)

    @Transaction
    open suspend fun update(tagEntity: TagEntity, oldLabel: String) {
        insert(tagEntity)
        updateRefs(oldLabel, tagEntity.label)
        delete(oldLabel)
    }

    @Query("DELETE FROM tag WHERE label = :label")
    abstract suspend fun delete(label: String)

    @Query("SELECT * FROM tag ORDER BY type ASC")
    abstract fun findAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE label in (:labels) ORDER BY type ASC")
    abstract fun resolve(labels: List<String>): Flow<List<TagEntity>>

}
