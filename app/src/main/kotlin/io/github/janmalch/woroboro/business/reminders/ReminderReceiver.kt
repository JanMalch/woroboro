package io.github.janmalch.woroboro.business.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.net.toFile
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.woroboro.business.ReminderRepository
import io.github.janmalch.woroboro.business.RoutineRepository
import io.github.janmalch.woroboro.business.findByReminder
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.Routine
import io.github.janmalch.woroboro.utils.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: ReminderScheduler

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var routines: RoutineRepository

    @Inject
    lateinit var notifications: ReminderNotifications

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderId = intent?.getReminderId() ?: return
        applicationScope.launch {
            handle(reminderId)
        }
    }

    private suspend fun handle(reminderId: UUID) {
        val reminder = repository.findOne(reminderId).firstOrNull()

        if (reminder == null) {
            Log.w(
                "ReminderReceiver",
                "Received reminder for $reminderId, but reminder doesn't exist. Removing reminder now ..."
            )
            scheduler.cancel(reminderId)
            repository.delete(reminderId)
            return
        }

        if (reminder.isDueNow()) {
            val image = try {
                routines.findByReminder(reminder).firstOrNull()
                    ?.takeUnless(List<Routine>::isEmpty)
                    ?.asSequence()
                    ?.flatMap { it.media }
                    ?.shuffled()
                    ?.firstOrNull()
                    ?.thumbnail
                    ?.let { Uri.parse(it).toFile().path }
                    ?.let(BitmapFactory::decodeFile)
            } catch (e: Exception) {
                Log.w(
                    "ReminderReceiver",
                    "Error while trying to create bitmap for reminder $reminderId.",
                    e
                )
                null
            }
            notifications.show(reminder, image)
        } else {
            Log.d(
                "ReminderReceiver",
                "Received reminder for $reminderId, but isn't due for now."
            )
        }

        // check if it's a repeating reminder, and if it is then check if until time is reached
        if (reminder.repeat?.until != null && LocalTime.now() >= reminder.repeat.until) {
            // if so then cancel ongoing and reschedule,
            // so that reminder doesn't trigger every x minutes but rather next day
            Log.d(
                "ReminderReceiver",
                "Rescheduling repeating reminder $reminderId for next day, because it is completed for today."
            )
            scheduler.schedule(reminder)
        }
    }

    companion object {
        const val INTENT_REMINDER_ID = "reminder_id"
    }
}


@VisibleForTesting
internal fun Reminder.isDueNow(now: LocalDateTime = LocalDateTime.now()): Boolean {
    if (now.dayOfWeek !in weekdays) return false
    val nowTime = now.toLocalTime()
    if (repeat == null) {
        return nowTime >= remindAt
    }
    // Add tolerance because the alarm might be delayed.
    // 15 minutes because that's the minimum interval.
    val validRange = remindAt..<repeat.until.plusMinutes(15)
    return nowTime in validRange
}

private fun Intent.getReminderId(): UUID? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(ReminderReceiver.INTENT_REMINDER_ID, UUID::class.java)
    } else {
        getSerializableExtra(ReminderReceiver.INTENT_REMINDER_ID) as UUID?
    }
