package com.studygo.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("task_title")
            ?: "Tienes una tarea pendiente"

        val subject = intent.getStringExtra("task_subject")
            ?: "StudyGo"

        val taskId = intent.getLongExtra("task_id", 0L)

        val channelId = "studygo_reminders"

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Recordatorios StudyGo",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Tienes una tarea pendiente!")
            .setContentText("$title - $subject")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Recuerda entregar: $title. Materia: $subject"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            taskId.hashCode(),
            notification
        )
    }
}