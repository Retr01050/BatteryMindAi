package com.creativeideas.batterymindai.ui.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onNext: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "welcome_scale"
    )

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(24.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon with animation
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4A9EFF).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BatteryChargingFull,
                contentDescription = null,
                tint = Color(0xFF4A9EFF),
                modifier = Modifier
                    .padding(24.dp)
                    .size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(800, delayMillis = 400)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to",
                    color = Color(0xFF9CA3AF),
                    fontSize = 18.sp
                )

                Text(
                    text = "BatteryMind AI",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(800, delayMillis = 800)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 800))
        ) {
            Text(
                text = "Your intelligent battery companion that learns from your usage patterns and helps optimize your device's performance.",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(800, delayMillis = 1200)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 1200))
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A9EFF)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800, delayMillis = 1600))
        ) {
            Text(
                text = "Let's set up your personalized battery optimization",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
