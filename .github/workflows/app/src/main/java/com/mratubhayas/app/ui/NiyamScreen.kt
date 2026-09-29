package com.mratubhayas.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mratubhayas.app.AlarmAlertActivity
import com.mratubhayas.app.model.AppPreferences
import com.mratubhayas.app.model.NiyamItem

@Composable
fun NiyamScreen(prefs: AppPreferences) {
    val context = LocalContext.current
    var niyamList by remember { mutableStateOf(prefs.getNiyamList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E11))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NIYAM DISCIPLINES",
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${niyamList.count { it.active }} Active Alarms",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Row {
                // Test Alarm button
                OutlinedButton(
                    onClick = {
                        val testIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                            putExtra("TITLE", "Test Niyam Alarm")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(testIntent)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Test Alert", color = Color(0xFFFFB300), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alarms List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(niyamList) { item ->
                NiyamCard(
                    item = item,
                    onToggle = { active ->
                        val updated = niyamList.map {
                            if (it.id == item.id) it.copy(active = active) else it
                        }
                        niyamList = updated.toMutableList()
                        prefs.saveNiyamList(updated)
                    },
                    onDelete = {
                        val updated = niyamList.filter { it.id != item.id }
                        niyamList = updated.toMutableList()
                        prefs.saveNiyamList(updated)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddNiyamDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, hour, minute ->
                val newItem = NiyamItem(title = title, hour = hour, minute = minute, active = true)
                val updated = niyamList.toMutableList().apply { add(newItem) }
                niyamList = updated
                prefs.saveNiyamList(updated)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun NiyamCard(item: NiyamItem, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val rh = if (item.hour % 12 == 0) 12 else item.hour % 12
    val amPm = if (item.hour >= 12) "PM" else "AM"
    val timeFormatted = String.format("%02d:%02d %s", rh, item.minute, amPm)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeFormatted,
                    color = if (item.active) Color.White else Color.Gray,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.title,
                    color = if (item.active) Color(0xFF00E5FF) else Color.DarkGray,
                    fontSize = 14.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
                Switch(
                    checked = item.active,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
fun AddNiyamDialog(onDismiss: () -> Unit, onAdd: (String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(6) }
    var minute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Niyam Discipline", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Discipline Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hour (0-23):", color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { hour = (hour - 1 + 24) % 24 }) { Text("-") }
                        Text("  $hour  ", color = Color.White, fontWeight = FontWeight.Bold)
                        Button(onClick = { hour = (hour + 1) % 24 }) { Text("+") }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Minute (0-59):", color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { minute = (minute - 5 + 60) % 60 }) { Text("-5") }
                        Text("  $minute  ", color = Color.White, fontWeight = FontWeight.Bold)
                        Button(onClick = { minute = (minute + 5) % 60 }) { Text("+5") }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, hour, minute)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Color(0xFF1E222D)
    )
}
