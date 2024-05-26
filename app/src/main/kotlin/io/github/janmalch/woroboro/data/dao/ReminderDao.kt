package io.github.janmalch.woroboro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.janmalch.woroboro.data.model.ReminderEntity
import io.github.janmalch.woroboro.data.model.ReminderEntityWithFilterTags
import io.github.janmalch.woroboro.data.model.ReminderFilterTagCrossRefEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReminderDao {

    @Transaction
    open suspend fun upsert(entity: ReminderEntity, filterTags: List<String>) {
        upsert(entity)
        deleteFilterTags(entity.id)
        insertFilterTags(
            filterTags.map {
                ReminderFilterTagCrossRefEntity(reminderId = entity.id, tagLabel = it)
            }
        )
    }

    @Upsert protected abstract suspend fun upsert(entity: ReminderEntity)

    @Insert
    protected abstract suspend fun insertFilterTags(refs: List<ReminderFilterTagCrossRefEntity>)

    @Query("DELETE FROM reminder_filter_tag WHERE reminder_id = :reminderId")
    protected abstract suspend fun deleteFilterTags(reminderId: UUID)

    @Query("DELETE FROM reminder WHERE id = :reminderId")
    abstract suspend fun delete(reminderId: UUID)

    @Transaction
    @Query("SELECT * FROM reminder ORDER BY reminder.name COLLATE NOCASE ASC")
    abstract fun findAll(): Flow<List<ReminderEntityWithFilterTags>>

    @Transaction
    @Query("SELECT * FROM reminder WHERE id = :id")
    abstract fun findOne(id: UUID): Flow<ReminderEntityWithFilterTags?>
}
