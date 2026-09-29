package com.mratubhayas.app

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mratubhayas.app.model.AppPreferences
import com.mratubhayas.app.ui.*

enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    DHYAN("Dhyan", Icons.Default.SelfImprovement),
    NIYAM("Niyam", Icons.Default.Alarm),
    SANSKAR("Sanskar", Icons.Default.NotificationsActive),
    SADHANA("Sadhana", Icons.Default.TrackChanges),
    PLANNER("Planner", Icons.Default.CalendarMonth)
}

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPreferences(this)

        // 1. Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 2. Request Exact Alarm permission on Android 12+ if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        // 3. Ensure all alarms are scheduled on launch
        prefs.rescheduleAllAlarms()

        setContent {
            MratuBhayasTheme {
                MainAppScaffold(prefs)
            }
        }
    }
}

@Composable
fun MainAppScaffold(prefs: AppPreferences) {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF161922),
                contentColor = Color(0xFF00E5FF)
            ) {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            selectedTextColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(prefs, onNavigateToDhyan = { selectedTab = AppTab.DHYAN })
                AppTab.DHYAN -> DhyanScreen()
                AppTab.NIYAM -> NiyamScreen(prefs)
                AppTab.SANSKAR -> SanskarScreen(prefs)
                AppTab.SADHANA -> SadhanaScreen(prefs)
                AppTab.PLANNER -> PlannerScreen(prefs)
            }
        }
    }
}
