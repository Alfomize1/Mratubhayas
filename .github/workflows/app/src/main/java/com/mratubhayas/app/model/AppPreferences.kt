package com.mratubhayas.app.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mratubhayas.app.receiver.NiyamAlarmReceiver
import com.mratubhayas.app.receiver.SanskarAlarmReceiver
import java.util.*

class AppPreferences(private val context: Context) {
    private val prefs = context.getSharedPreferences("mratu_bhayas_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // --- NIYAM LIST ---
    fun getNiyamList(): MutableList<NiyamItem> {
        val json = prefs.getString("niyam_list", null) ?: return mutableListOf(
            NiyamItem(title = "Morning Sadhana", hour = 6, minute = 0, active = true),
            NiyamItem(title = "Evening Meditation", hour = 18, minute = 30, active = true)
        )
        val type = object : TypeToken<MutableList<NiyamItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveNiyamList(list: List<NiyamItem>) {
        prefs.edit().putString("niyam_list", gson.toJson(list)).apply()
        rescheduleAllAlarms()
    }

    // --- SANSKAR LIST ---
    fun getSanskarList(): MutableList<SanskarItem> {
        val json = prefs.getString("sanskar_list", null) ?: return mutableListOf(
            SanskarItem(title = "Breathe & Smile", intervalMin = 30, active = true, startHour = 8, endHour = 22)
        )
        val type = object : TypeToken<MutableList<SanskarItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveSanskarList(list: List<SanskarItem>) {
        prefs.edit().putString("sanskar_list", gson.toJson(list)).apply()
        rescheduleAllAlarms()
    }

    // --- SADHANA LIST ---
    fun getSadhanaList(): MutableList<SadhanaItem> {
        val json = prefs.getString("sadhana_list", null) ?: return mutableListOf(
            SadhanaItem(title = "Mratu Dhyan Anushthan", targetDays = 21, active = true)
        )
        val type = object : TypeToken<MutableList<SadhanaItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveSadhanaList(list: List<SadhanaItem>) {
        prefs.edit().putString("sadhana_list", gson.toJson(list)).apply()
    }

    // --- TRACKER LIST ---
    fun getTrackerList(): MutableList<TrackerItem> {
        val json = prefs.getString("tracker_list", null) ?: return mutableListOf(
            TrackerItem(title = "Daily Naam Japa", targetDays = 40, completedDays = 5, missedDays = 1)
        )
        val type = object : TypeToken<MutableList<TrackerItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveTrackerList(list: List<TrackerItem>) {
        prefs.edit().putString("tracker_list", gson.toJson(list)).apply()
    }

    // --- PLANNER EVENTS ---
    fun getPlannerEvents(): MutableList<PlannerEvent> {
        val json = prefs.getString("planner_list", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<PlannerEvent>>() {}.type
        return gson.fromJson(json, type)
    }

    fun savePlannerEvents(list: List<PlannerEvent>) {
        prefs.edit().putString("planner_list", gson.toJson(list)).apply()
    }

    // --- NOTES ---
    fun getNotes(): MutableList<NoteItem> {
        val json = prefs.getString("notes_list", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<NoteItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveNotes(list: List<NoteItem>) {
        prefs.edit().putString("notes_list", gson.toJson(list)).apply()
    }

    // --- LOGS ---
    fun getLogs(): MutableList<DailyLogItem> {
        val json = prefs.getString("daily_logs", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<DailyLogItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addLog(log: DailyLogItem) {
        val logs = getLogs()
        logs.add(0, log)
        if (logs.size > 100) logs.removeAt(logs.size - 1)
        prefs.edit().putString("daily_logs", gson.toJson(logs)).apply()
    }

    // ================= EXACT-SECOND BACKGROUND ALARM SCHEDULING =================
    fun rescheduleAllAlarms() {
        val niyamList = getNiyamList()
        for (item in niyamList) {
            if (item.active) scheduleExactNiyamAlarm(item)
            else cancelNiyamAlarm(item)
        }

        val sanskarList = getSanskarList()
        for (item in sanskarList) {
            if (item.active) scheduleSanskarAlarm(item)
            else cancelSanskarAlarm(item)
        }
    }

    fun scheduleExactNiyamAlarm(item: NiyamItem) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, item.hour)
            set(Calendar.MINUTE, item.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1) // Schedule for tomorrow if time has passed
            }
        }

        val intent = Intent(context, NiyamAlarmReceiver::class.java).apply {
            putExtra("NIYAM_ID", item.id)
            putExtra("TITLE", item.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setAlarmClock is Android's highest priority alarm API.
        // It bypasses Doze Mode and deep sleep, firing at the EXACT millisecond!
        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelNiyamAlarm(item: NiyamItem) {
        val intent = Intent(context, NiyamAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleSanskarAlarm(item: SanskarItem) {
        val triggerAt = System.currentTimeMillis() + (item.intervalMin * 60 * 1000L)
        val intent = Intent(context, SanskarAlarmReceiver::class.java).apply {
            putExtra("SANSKAR_ID", item.id)
            putExtra("TITLE", item.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAt, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelSanskarAlarm(item: SanskarItem) {
        val intent = Intent(context, SanskarAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
