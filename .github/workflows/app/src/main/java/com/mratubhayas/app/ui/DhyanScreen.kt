package com.mratubhayas.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mratubhayas.app.audio.NokiaToneEngine
import kotlinx.coroutines.delay

@Composable
fun DhyanScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var phaseIndex by remember { mutableIntStateOf(0) } // 0: Inhale, 1: Hold In, 2: Exhale, 3: Hold Out
    var secondsLeftInPhase by remember { mutableIntStateOf(4) }
    val phaseDurations = listOf(4, 4, 4, 4) // Standard 4-4-4-4 Sama Vritti box breathing
    val phaseNames = listOf("INHALE", "HOLD IN", "EXHALE", "HOLD OUT")

    // Animated breathing scale (1.0 to 1.6)
    val targetScale = when (phaseIndex) {
        0 -> 1.6f // Inhaling expands
        1 -> 1.6f // Holding expanded
        2 -> 1.0f // Exhaling contracts
        else -> 1.0f // Holding contracted
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isRunning) targetScale else 1.0f,
        animationSpec = tween(
            durationMillis = if (isRunning) phaseDurations[phaseIndex] * 1000 else 400,
            easing = LinearEasing
        ),
        label = "BreathingScale"
    )

    // Breathing Engine Coroutine
    LaunchedEffect(isRunning) {
        if (isRunning) {
            phaseIndex = 0
            while (isRunning) {
                val currentDur = phaseDurations[phaseIndex]
                secondsLeftInPhase = currentDur
                
                // Play acoustic frequency tone at phase transition (like ESP32)
                val freq = 440 + (phaseIndex * 220)
                NokiaToneEngine.playBeep(freq, 100)

                for (s in currentDur downTo 1) {
                    secondsLeftInPhase = s
                    delay(1000L)
                }

                phaseIndex = (phaseIndex + 1) % 4
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0E11))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "DHYAN & PRANAYAMA",
                color = Color(0xFF00E5FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Box Breathing (4-4-4-4)",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        // Live Animated Breathing Circle
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(animatedScale),
            contentAlignment = Alignment.Center
        ) {
            // Outer glowing aura
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), CircleShape)
            )

            // Inner circle with phase details
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(Color(0xFF161922), CircleShape)
                    .border(1.dp, Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isRunning) phaseNames[phaseIndex] else "READY",
                        color = Color(0xFFFFB300),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isRunning) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${secondsLeftInPhase}s",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Action Buttons
        Button(
            onClick = { isRunning = !isRunning },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFFFF5252) else Color(0xFF00E5FF)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRunning) "STOP SESSION" else "START PRANAYAMA",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
