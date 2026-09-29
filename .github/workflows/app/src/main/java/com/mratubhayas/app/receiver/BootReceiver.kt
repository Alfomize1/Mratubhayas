package com.mratubhayas.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mratubhayas.app.model.AppPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = AppPreferences(context)
            prefs.rescheduleAllAlarms()
        }
    }
}
