package com.mratubhayas.app.model

data class NiyamItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val hour: Int,
    val minute: Int,
    var active: Boolean = true,
    var currentSnoozeCount: Int = 0,
    var lastTriggeredEpochDay: Long = 0
)

data class SanskarItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val intervalMin: Int = 30, // 15, 30, 60 minutes
    var active: Boolean = true,
    var startHour: Int = 8,
    var endHour: Int = 22
)

data class SadhanaItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val startTimestamp: Long = System.currentTimeMillis() / 1000L,
    val targetDays: Int = 21,
    var active: Boolean = true
)

data class TrackerItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val targetDays: Int = 50,
    var completedDays: Int = 0,
    var missedDays: Int = 0,
    var lastCheckedEpochDay: Long = 0
)

data class PlannerEvent(
    val id: Long = System.currentTimeMillis(),
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val title: String,
    var active: Boolean = true
)

data class NoteItem(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyLogItem(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val timeStr: String,
    val status: String, // "DONE", "MISSED", "SNOOZED"
    val type: String    // "NIYAM", "SANSKAR", "ALARM"
)
