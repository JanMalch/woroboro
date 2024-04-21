package io.github.janmalch.woroboro.business.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.VisibleForTesting
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.woroboro.models.Reminder
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminderId: UUID)
}

class AndroidReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.INTENT_REMINDER_ID, reminder.id)
        }

        val next = closestInstantBy(reminder.weekdays, time = reminder.remindAt)
        Log.d(
            "AndroidReminderScheduler",
            "Setting up ${reminder.id}; next reminder at ${
                LocalDateTime.ofInstant(
                    next,
                    ZoneId.systemDefault()
                )
            }."
        )
        if (reminder.repeat != null) {
            // If it repeats within a day we repeat it with the reminder's repeat duration.
            // The receiver will check if repeating for the day can be cancelled.
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                next.toEpochMilli(),
                reminder.repeat.every.inWholeMilliseconds,
                createPendingIntent(reminder.requestCode, intent)
            )
        } else {
            // If it doesn't repeat within a day, we can simply use
            // the differences in days for repeating and
            // the receiver doesn't have to cancel it.
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                next.toEpochMilli(),
                shortestDurationBetween(reminder.weekdays).inWholeMilliseconds,
                createPendingIntent(reminder.requestCode, intent)
            )
        }
    }

    override fun cancel(reminderId: UUID) {
        alarmManager.cancel(createPendingIntent(reminderId.hashCode()))
        Log.d("AndroidReminderScheduler", "Cancelled reminder with ID $reminderId")
    }

    private fun createPendingIntent(
        requestCode: Int,
        intent: Intent = Intent(context, ReminderReceiver::class.java),
    ) = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private val Reminder.requestCode
        get() = id.hashCode()
}


@VisibleForTesting
internal fun shortestDurationBetween(
    weekdays: Set<DayOfWeek>,
): Duration {
    require(weekdays.isNotEmpty()) {
        "At least one weekday must be provided."
    }
    if (weekdays.size == 1) {
        return 7.days
    }
    // TODO: make this smarter
    return 1.days
}


@VisibleForTesting
internal fun closestInstantBy(
    weekdays: Set<DayOfWeek>,
    time: LocalTime,
    zoneId: ZoneId = ZoneId.systemDefault(),
    now: Instant = Instant.now(),
): Instant {
    return weekdays
        .map { nextInstantOf(it, time, zoneId = zoneId, now = now) }
        .minBy { it.toEpochMilli() - now.toEpochMilli() }
}

@VisibleForTesting
internal fun nextInstantOf(
    dayOfWeek: DayOfWeek,
    time: LocalTime,
    zoneId: ZoneId = ZoneId.systemDefault(),
    now: Instant = Instant.now(),
): Instant {
    val nowLdt = LocalDateTime.ofInstant(now, zoneId)
    return nowLdt
        .with(TemporalAdjusters.nextOrSame(dayOfWeek))
        .toInstantWithTime(time, zoneId)
        .takeIf { it > now }
        ?: nowLdt
            .with(TemporalAdjusters.next(dayOfWeek))
            .toInstantWithTime(time, zoneId)
}

private fun LocalDateTime.toInstantWithTime(
    time: LocalTime,
    zoneId: ZoneId,
): Instant = with(time)
    .atZone(zoneId)
    .toInstant()
