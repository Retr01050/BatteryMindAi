package com.creativeideas.batterymindai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeideas.batterymindai.data.models.DischargeInfo
import com.creativeideas.batterymindai.data.models.DischargeLevel

@Composable
fun DischargeRateBar(
    dischargeInfo: DischargeInfo,
    showDetails: Boolean,
    onToggleDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // FIX: Usa i colori del tema
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Discharge Rate",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onToggleDetails) {
                    Icon(
                        imageVector = if (showDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showDetails) "Hide details" else "Show details",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DischargeRateVisualization(
                currentRate = dischargeInfo.currentRate,
                averageRate = dischargeInfo.averageRate,
                status = dischargeInfo.status
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getStatusEmoji(dischargeInfo.status),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getStatusText(dischargeInfo.status),
                        color = getStatusColor(dischargeInfo.status),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = dischargeInfo.estimatedTimeRemaining,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    // Qui andrebbe il contenuto dei dettagli, se presente
                }
            }
        }
    }
}

@Composable
private fun DischargeRateVisualization(
    currentRate: Float,
    averageRate: Float,
    status: DischargeLevel
) {
    val maxRate = 50f
    val currentProgress = (currentRate / maxRate).coerceIn(0f, 1f)
    val averageProgress = (averageRate / maxRate).coerceIn(0f, 1f)

    Column {
        Text(
            text = "Current: ${currentRate.toInt()} units/h",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(currentProgress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(getStatusColor(status))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Average: ${averageRate.toInt()} units/h",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(averageProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray)
            )
        }
    }
}


private fun getStatusEmoji(status: DischargeLevel): String {
    return when (status) {
        DischargeLevel.LOW -> "🟢"
        DischargeLevel.MEDIUM -> "🟡"
        DischargeLevel.HIGH -> "🔴"
    }
}

private fun getStatusText(status: DischargeLevel): String {
    return when (status) {
        DischargeLevel.LOW -> "Normal Discharge"
        DischargeLevel.MEDIUM -> "Moderate Discharge"
        DischargeLevel.HIGH -> "High Discharge"
    }
}

private fun getStatusColor(status: DischargeLevel): Color {
    return when (status) {
        DischargeLevel.LOW -> Color(0xFF4CAF50)
        DischargeLevel.MEDIUM -> Color(0xFFFF9800)
        DischargeLevel.HIGH -> Color(0xFFF44336)
    }
}