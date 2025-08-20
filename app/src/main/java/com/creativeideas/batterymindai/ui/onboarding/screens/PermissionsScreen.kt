package com.creativeideas.batterymindai.ui.onboarding.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.creativeideas.batterymindai.ui.onboarding.viewmodels.PermissionsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PermissionsScreen(
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    var showFinishAnimation by remember { mutableStateOf(false) }
    var isFinishing by remember { mutableStateOf(false) }

    // FIX: Aggiunto observer per aggiornare la UI quando si torna alla schermata.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val finishScale by animateFloatAsState(
        targetValue = if (showFinishAnimation) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "finish_scale"
    )

    val finishAlpha by animateFloatAsState(
        targetValue = if (showFinishAnimation) 0f else 1f,
        animationSpec = tween(1000),
        label = "finish_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBack,
                enabled = !isFinishing
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (showFinishAnimation) {
            Box(
                modifier = Modifier.fillMaxSize().scale(finishScale),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(1f - finishAlpha)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(120.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Setup Complete!", color = MaterialTheme.colorScheme.onBackground, fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(text = "Welcome to BatteryMind AI", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Grant Permissions", color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "We need these permissions to optimize your battery effectively", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(48.dp))

            PermissionCard(
                title = "Usage Access",
                description = "Monitor app usage to provide battery insights",
                icon = Icons.Default.Analytics,
                isGranted = uiState.usageAccessGranted,
                isRequired = true,
                onGrant = {
                    // FIX: Prima triggera la registrazione, poi apri le impostazioni.
                    scope.launch {
                        viewModel.triggerUsageStatsRegistration()
                        delay(100) // Piccolo delay per sicurezza
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionCard(
                title = "Battery Optimization",
                description = "Allow the app to run in background for monitoring",
                icon = Icons.Default.BatteryChargingFull,
                isGranted = uiState.batteryOptimizationDisabled,
                isRequired = false,
                onGrant = {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { uiState.permissionProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "${(uiState.permissionProgress * 100).toInt()}% Complete", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (uiState.canFinish) {
                        isFinishing = true
                        showFinishAnimation = true
                        lifecycleOwner.lifecycleScope.launch {
                            viewModel.completeOnboarding()
                            delay(2000)
                            onFinish()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState.canFinish && !isFinishing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isFinishing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(text = if (uiState.canFinish) "Finish Setup" else "Grant Required Permissions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (!uiState.canFinish) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Usage Access permission is required to continue", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFF1B5E20).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Required",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Grant",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}