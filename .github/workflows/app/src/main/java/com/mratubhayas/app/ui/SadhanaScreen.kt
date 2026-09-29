package com.mratubhayas.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mratubhayas.app.model.AppPreferences
import com.mratubhayas.app.model.SadhanaItem
import com.mratubhayas.app.model.TrackerItem

@Composable
fun SadhanaScreen(prefs: AppPreferences) {
    var sadhanaList by remember { mutableStateOf(prefs.getSadhanaList()) }
    var trackerList by remember { mutableStateOf(prefs.getTrackerList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sadhana, 1: Tracker

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E11))
            .padding(16.dp)
    ) {
        // Tab selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF161922),
            contentColor = Color(0xFF00E5FF)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("SADHANA CHALLENGES", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("HABIT TRACKER", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // SADHANA LIST
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sadhanaList) { item ->
                    SadhanaCard(item = item)
                }
            }
        } else {
            // TRACKER LIST
            val currentEpochDay = System.currentTimeMillis() / (86400 * 1000L)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trackerList) { item ->
                    TrackerCard(
                        item = item,
                        currentEpochDay = currentEpochDay,
                        onMarkToday = {
                            val updated = trackerList.map {
                                if (it.id == item.id) {
                                    it.copy(
                                        completedDays = it.completedDays + 1,
                                        lastCheckedEpochDay = currentEpochDay
                                    )
                                } else it
                            }
                            trackerList = updated.toMutableList()
                            prefs.saveTrackerList(updated)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SadhanaCard(item: SadhanaItem) {
    val nowSec = System.currentTimeMillis() / 1000L
    val elapsedDays = if (nowSec >= item.startTimestamp) {
        ((nowSec - item.startTimestamp) / 86400L).toInt().coerceAtMost(item.targetDays)
    } else 0
    val progress = if (item.targetDays > 0) elapsedDays.toFloat() / item.targetDays.toFloat() else 0f
    val pct = (progress * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "[$pct%]",
                    color = Color(0xFFFFB300),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Day $elapsedDays of ${item.targetDays}",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rounded Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF262A34),
            )
        }
    }
}

@Composable
fun TrackerCard(item: TrackerItem, currentEpochDay: Long, onMarkToday: () -> Unit) {
    val isMarkedToday = item.lastCheckedEpochDay == currentEpochDay
    val totalProgress = if (item.targetDays > 0) item.completedDays.toFloat() / item.targetDays.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Done: ${item.completedDays}d | Miss: ${item.missedDays}d | Goal: ${item.targetDays}d",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                if (isMarkedToday) {
                    FilledTonalButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Done", color = Color(0xFF00E5FF))
                    }
                } else {
                    Button(
                        onClick = onMarkToday,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("Mark Today", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { totalProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF262A34),
            )
        }
    }
}
