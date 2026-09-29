package com.mratubhayas.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mratubhayas.app.model.AppPreferences
import com.mratubhayas.app.model.SanskarItem

@Composable
fun SanskarScreen(prefs: AppPreferences) {
    var sanskarList by remember { mutableStateOf(prefs.getSanskarList()) }
    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "SANSKAR MINDFUL CHIMES",
                    color = Color(0xFF00E5FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Periodic Interval Reminders",
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

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sanskarList) { item ->
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
                                text = item.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Every ${item.intervalMin}m | ${item.startHour}:00 - ${item.endHour}:00",
                                color = Color(0xFFFFB300),
                                fontSize = 13.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val updated = sanskarList.filter { it.id != item.id }
                                sanskarList = updated.toMutableList()
                                prefs.saveSanskarList(updated)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                            Switch(
                                checked = item.active,
                                onCheckedChange = { active ->
                                    val updated = sanskarList.map {
                                        if (it.id == item.id) it.copy(active = active) else it
                                    }
                                    sanskarList = updated.toMutableList()
                                    prefs.saveSanskarList(updated)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00E5FF),
                                    checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var intervalMin by remember { mutableIntStateOf(30) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Sanskar Chime", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Chime Title") },
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
                        Text("Interval:", color = Color.Gray)
                        Row {
                            listOf(15, 30, 60).forEach { mins ->
                                FilterChip(
                                    selected = intervalMin == mins,
                                    onClick = { intervalMin = mins },
                                    label = { Text("${mins}m") },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        val newSanskar = SanskarItem(title = title, intervalMin = intervalMin, active = true)
                        val updated = sanskarList.toMutableList().apply { add(newSanskar) }
                        sanskarList = updated
                        prefs.saveSanskarList(updated)
                        showAddDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
            containerColor = Color(0xFF1E222D)
        )
    }
}
