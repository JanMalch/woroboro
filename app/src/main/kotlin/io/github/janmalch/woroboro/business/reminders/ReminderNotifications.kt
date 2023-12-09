package io.github.janmalch.woroboro.business.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.woroboro.MainActivity
import io.github.janmalch.woroboro.models.Reminder
import io.github.janmalch.woroboro.models.RoutineFilter
import javax.inject.Inject


interface ReminderNotifications {
    fun show(reminder: Reminder, image: Bitmap? = null)
}

class AndroidReminderNotifications @Inject constructor(
    @ApplicationContext private val context: Context,
) : ReminderNotifications {
    override fun show(reminder: Reminder, image: Bitmap?) {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            // TODO: revisit flags
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(INTENT_EXTRA_FILTER, reminder.filter)
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(reminder.name)
            .setContentText("Es ist Zeit für eine deiner Routinen!") // TODO: i18n
            .setLargeIcon(image)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // remove on tap
            .setOnlyAlertOnce(false) // alert again if same reminder triggers again
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(reminder.id.hashCode(), notification)
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Woroboro Reminders" // TODO: i18n
        val descriptionText =
            "Benachrichtigungen für deine selbsterstellten Erinnerungen." // TODO: i18n
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "Woroboro Reminders"
        private const val INTENT_EXTRA_FILTER = "filters"

        fun Bundle.getRoutineFilter(): RoutineFilter? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelable(INTENT_EXTRA_FILTER, RoutineFilter::class.java)
            } else {
                getParcelable(INTENT_EXTRA_FILTER)
            }
    }
}