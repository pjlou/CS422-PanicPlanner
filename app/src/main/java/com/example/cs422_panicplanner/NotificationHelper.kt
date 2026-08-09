package com.example.cs422_panicplanner

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    const val CHANNEL_ID = "event_reminders"

    private const val CHANNEL_NAME =
        "Event reminders"

    private const val CHANNEL_DESCRIPTION =
        "Notifications for upcoming PanicPlanner events"

    fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    fun showEventNotification(
        context: Context,
        eventId: Int,
        eventTitle: String,
        eventDescription: String
    ) {

        createNotificationChannel(context)

        /*
         * When the user taps the notification, open the
         * detail page for that specific event.
         */
        val openEventIntent =
            Intent(
                context,
                EventActivity::class.java
            ).apply {

                putExtra("EVENT_ID", eventId)

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                eventId,
                openEventIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(eventTitle)
                .setContentText(eventDescription)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(eventDescription)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_REMINDER
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        /*
         * Android 13+ requires POST_NOTIFICATIONS permission.
         */
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.TIRAMISU ||

            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            NotificationManagerCompat
                .from(context)
                .notify(
                    eventId,
                    notification
                )
        }
    }
}