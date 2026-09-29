package com.mratubhayas.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mratubhayas.app.model.AppPreferences
import com.mratubhayas.app.model.PlannerEvent
import java.util.*

@Composable
fun PlannerScreen(prefs: AppPreferences) {
    var plannerEvents by remember { mutableStateOf(prefs.getPlannerEvents()) }
    val calendar = remember { Calendar.getInstance() }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    var showAddDialog by remember { mutableStateOf(false) }

    val daysInMonth = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, currentMonth)
            set(Calendar.YEAR, currentYear)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val dayEvents = plannerEvents.filter {
        it.day == selectedDay && it.month == currentMonth + 1 && it.year == currentYear
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E11))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EVENT PLANNER",
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Selected: Day $selectedDay",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF00E5FF),
                contentColor = Color.Black,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Calendar Day Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val startDay = (selectedDay - 2).coerceAtLeast(1)
            val endDay = (selectedDay + 2).coerceAtMost(daysInMonth)

            for (d in startDay..endDay) {
                val isSelected = d == selectedDay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .background(
                            if (isSelected) Color(0xFF00E5FF) else Color(0xFF161922),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedDay = d },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DAY",
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "$d",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Events on this Day (${dayEvents.size}):",
            color = Color(0xFFFFB300),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (dayEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No events scheduled for Day $selectedDay", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dayEvents) { event ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(event.title, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                String.format("%02d:%02d", event.hour, event.minute),
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var eventTitle by remember { mutableStateOf("") }
        var eventHour by remember { mutableIntStateOf(9) }
        var eventMin by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Event on Day $selectedDay", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Event Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (eventTitle.isNotBlank()) {
                        val newEv = PlannerEvent(
                            year = currentYear,
                            month = currentMonth + 1,
                            day = selectedDay,
                            hour = eventHour,
                            minute = eventMin,
                            title = eventTitle
                        )
                        val updated = plannerEvents.toMutableList().apply { add(newEv) }
                        plannerEvents = updated
                        prefs.savePlannerEvents(updated)
                        showAddDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
            containerColor = Color(0xFF1E222D)
        )
    }
}
