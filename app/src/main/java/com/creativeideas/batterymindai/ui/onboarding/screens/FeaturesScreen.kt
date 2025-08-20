package com.creativeideas.batterymindai.ui.onboarding.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun FeaturesScreen(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var currentFeatureIndex by remember { mutableIntStateOf(0) }

    val features = remember {
        listOf(
            Feature(
                icon = Icons.Default.Psychology,
                title = "AI-Powered Analysis",
                description = "Advanced machine learning algorithms analyze your battery usage patterns and provide intelligent recommendations.",
                color = Color(0xFF4A9EFF)
            ),
            Feature(
                icon = Icons.Default.Speed,
                title = "Real-time Monitoring",
                description = "Monitor your battery health, temperature, and discharge rate in real-time with detailed analytics.",
                color = Color(0xFF4CAF50)
            ),
            Feature(
                icon = Icons.Default.AutoFixHigh,
                title = "Smart Optimization",
                description = "Automatically optimize your device settings based on usage patterns and battery condition.",
                color = Color(0xFFFF9800)
            ),
            Feature(
                icon = Icons.Default.Insights,
                title = "Detailed Insights",
                description = "Get comprehensive reports on app usage, system performance, and battery consumption trends.",
                color = Color(0xFF9C27B0)
            )
        )
    }

    LaunchedEffect(Unit) {
        while (currentFeatureIndex < features.size - 1) {
            delay(3000)
            currentFeatureIndex++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D29))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Powerful Features",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Discover what makes BatteryMind AI special",
            color = Color(0xFF9CA3AF),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Feature showcase
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(features) { index, feature ->
                val isActive = index <= currentFeatureIndex

                AnimatedVisibility(
                    visible = isActive,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(500, delayMillis = index * 200)
                    ) + fadeIn(
                        animationSpec = tween(500, delayMillis = index * 200)
                    )
                ) {
                    FeatureCard(
                        feature = feature,
                        isHighlighted = index == currentFeatureIndex
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            features.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index <= currentFeatureIndex) Color(0xFF4A9EFF) else Color(0xFF3A3D4A),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Continue button
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
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FeatureCard(
    feature: Feature,
    isHighlighted: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "feature_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                feature.color.copy(alpha = 0.1f)
            else
                Color(0xFF2A2D3A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = feature.color.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.color,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = feature.description,
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private data class Feature(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)
