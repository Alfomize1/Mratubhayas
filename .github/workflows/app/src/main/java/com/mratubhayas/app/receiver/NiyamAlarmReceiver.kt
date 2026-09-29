package com.mratubhayas.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mratubhayas.app.AlarmAlertActivity
import com.mratubhayas.app.R

class NiyamAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TITLE") ?: "Niyam Due!"
        val niyamId = intent.getLongExtra("NIYAM_ID", 0L)

        // 1. Acquire temporary WakeLock so CPU stays awake during alert launch
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MratuBhayas:AlarmWakeLock"
        )
        wakeLock.acquire(15000L) // Release after 15 seconds

        // 2. Launch Full-Screen Lock Screen Alarm Activity
        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("NIYAM_ID", niyamId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            niyamId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Post High Priority Notification with Full Screen Intent
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "niyam_alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Niyam Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent full-screen alarms for Niyam disciplines"
                enableVibration(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MratuBhayas Niyam Alert")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()

        notificationManager.notify(niyamId.toInt(), notification)

        // Also launch directly to ensure screen turns on
        try {
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {}
    }
}
