package com.mratubhayas.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mratubhayas.app.R
import com.mratubhayas.app.audio.NokiaToneEngine
import com.mratubhayas.app.model.AppPreferences
import java.util.*

class SanskarAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TITLE") ?: "Sanskar Reminder"
        val sanskarId = intent.getLongExtra("SANSKAR_ID", 0L)
        val prefs = AppPreferences(context)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val sanskar = prefs.getSanskarList().find { it.id == sanskarId }

        if (sanskar != null && sanskar.active) {
            // Check active hours window (e.g. 08:00 to 22:00)
            if (currentHour in sanskar.startHour..sanskar.endHour) {
                // Play mindful acoustic chime
                NokiaToneEngine.playTone(0, loop = false)

                // Show notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "sanskar_channel"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Sanskar Chimes",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Mindful hourly reminder chimes"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("MratuBhayas Sanskar")
                    .setContentText(title)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify((sanskarId + 20000).toInt(), notification)
            }

            // Reschedule next occurrence
            prefs.scheduleSanskarAlarm(sanskar)
        }
    }
}
