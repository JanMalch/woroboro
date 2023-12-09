package io.github.janmalch.woroboro.business.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import io.github.janmalch.woroboro.business.ReminderRepository
import io.github.janmalch.woroboro.utils.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: ReminderScheduler

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(
                "BootCompletedReceiver",
                "Received boot completed event. Rescheduling all reminders."
            )
            applicationScope.launch {
                val reminders = repository.findAll().first()
                reminders.forEach(scheduler::schedule)
            }
        }
    }
}
