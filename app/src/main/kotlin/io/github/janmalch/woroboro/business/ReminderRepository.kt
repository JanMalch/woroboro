package io.github.janmalch.woroboro.business

import androidx.room.withTransaction
import io.github.janmalch.woroboro.business.reminders.ReminderScheduler
import io.github.janmalch.woroboro.data.AppDatabase
import io.github.janmalch.woroboro.data.dao.ReminderDao
import io.github.janmalch.woroboro.data.model.ReminderEntityWithFilterTags
import io.github.janmalch.woroboro.data.model.asModel
import io.github.janmalch.woroboro.models.Reminder
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface ReminderRepository {
    fun findAll(): Flow<List<Reminder>>

    fun findOne(id: UUID): Flow<Reminder?>

    suspend fun insert(reminder: Reminder): UUID

    suspend fun update(reminder: Reminder): UUID

    suspend fun delete(reminderId: UUID)
}

suspend fun ReminderRepository.activate(reminderId: UUID) {
    val reminder =
        requireNotNull(findOne(reminderId).first()) {
            "'$reminderId' is not an ID for an existing reminder"
        }
    update(reminder.copy(isActive = true))
}

suspend fun ReminderRepository.deactivate(reminderId: UUID) {
    val reminder =
        requireNotNull(findOne(reminderId).first()) {
            "'$reminderId' is not an ID for an existing reminder"
        }
    update(reminder.copy(isActive = false))
}

class ReminderRepositoryImpl
@Inject
constructor(
    private val database: AppDatabase,
    private val reminderDao: ReminderDao,
    private val reminderScheduler: ReminderScheduler,
) : ReminderRepository {
    override fun findAll(): Flow<List<Reminder>> {
        return reminderDao.findAll().map { list -> list.map(ReminderEntityWithFilterTags::asModel) }
    }

    override fun findOne(id: UUID): Flow<Reminder?> {
        return reminderDao.findOne(id).map { it?.asModel() }
    }

    override suspend fun insert(reminder: Reminder): UUID {
        val model = reminder.copy(id = UUID.randomUUID())
        val (entity, filterTags) = model.asEntities()
        reminderDao.upsert(entity, filterTags)
        if (reminder.isActive) {
            reminderScheduler.schedule(model)
        }
        return model.id
    }

    override suspend fun update(reminder: Reminder): UUID {
        return database.withTransaction {
            // delete and insert for new ID,
            // so that notifications don't get weird (?)
            delete(reminder.id)
            insert(reminder)
        }
    }

    override suspend fun delete(reminderId: UUID) {
        reminderScheduler.cancel(reminderId)
        reminderDao.delete(reminderId)
    }
}
