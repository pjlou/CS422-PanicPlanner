package com.example.cs422_panicplanner

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class EventNotificationWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {

    override fun doWork(): Result {

        val eventId =
            inputData.getInt(KEY_EVENT_ID, -1)

        if (eventId == -1) {
            return Result.failure()
        }

        val eventTitle =
            inputData.getString(KEY_EVENT_TITLE)
                ?: "Upcoming event"

        val eventDescription =
            inputData.getString(KEY_EVENT_DESCRIPTION)
                ?.takeIf { it.isNotBlank() }
                ?: "You have an event coming up."

        NotificationHelper.showEventNotification(
            context = applicationContext,
            eventId = eventId,
            eventTitle = eventTitle,
            eventDescription = eventDescription
        )

        return Result.success()
    }

    companion object {
        const val KEY_EVENT_ID = "event_id"
        const val KEY_EVENT_TITLE = "event_title"
        const val KEY_EVENT_DESCRIPTION = "event_description"
    }
}