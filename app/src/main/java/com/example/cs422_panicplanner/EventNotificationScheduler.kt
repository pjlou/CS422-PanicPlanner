package com.example.cs422_panicplanner

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object EventNotificationScheduler {

    //event reminds user 10 minutes before it starts.
    const val DEFAULT_REMINDER_MINUTES_BEFORE = 10L

    fun scheduleEventNotification(
        context: Context,
        eventId: Int,
        eventTitle: String,
        eventDescription: String,
        eventStartTimeMillis: Long,
        reminderMinutesBefore: Long = DEFAULT_REMINDER_MINUTES_BEFORE
    ) {
        val now = System.currentTimeMillis()

        //don't schedule reminder if event already started.
        if (eventStartTimeMillis <= now) {
            cancelEventNotification(context, eventId)
            return
        }

        val requestedReminderTime =
            eventStartTimeMillis -
                    TimeUnit.MINUTES.toMillis(reminderMinutesBefore)

        /*
         * If the event is created less than 10 minutes before it starts,
         * schedule the notification almost immediately instead of
         * silently skipping it.
         */
        val reminderTimeMillis =
            maxOf(requestedReminderTime, now + 1_000L)

        val delayMillis = reminderTimeMillis - now

        val inputData = Data.Builder()
            .putInt(
                EventNotificationWorker.KEY_EVENT_ID,
                eventId
            )
            .putString(
                EventNotificationWorker.KEY_EVENT_TITLE,
                eventTitle
            )
            .putString(
                EventNotificationWorker.KEY_EVENT_DESCRIPTION,
                eventDescription
            )
            .build()

        val notificationWork =
            OneTimeWorkRequestBuilder<EventNotificationWorker>()
                .setInitialDelay(
                    delayMillis,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(inputData)
                .addTag(notificationTag(eventId))
                .build()

        /*
         * Each event gets unique WorkManager work.
         *
         * If the event gets edited, REPLACE removes the old scheduled
         * reminder and replaces it with the new one.
         */
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName(eventId),
            ExistingWorkPolicy.REPLACE,
            notificationWork
        )
    }

    fun cancelEventNotification(
        context: Context,
        eventId: Int
    ) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(uniqueWorkName(eventId))
    }

    private fun uniqueWorkName(eventId: Int): String {
        return "event_notification_$eventId"
    }

    private fun notificationTag(eventId: Int): String {
        return "event_notification_tag_$eventId"
    }
}