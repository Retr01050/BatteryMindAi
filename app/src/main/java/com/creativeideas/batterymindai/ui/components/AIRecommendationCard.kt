package com.creativeideas.batterymindai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creativeideas.batterymindai.data.models.AIRecommendation

@Composable
fun AIRecommendationCard(
    recommendation: AIRecommendation,
    onAccept: (AIRecommendation) -> Unit,
    onDismiss: (AIRecommendation) -> Unit,
    modifier: Modifier = Modifier,
    showActions: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // FIX: Usa i colori del tema per lo sfondo della card.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = recommendation.title,
                        // FIX: Usa i colori del tema per il testo.
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.description,
                        // FIX: Usa i colori del tema per il testo secondario.
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = getPriorityColor(recommendation.priority)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = recommendation.priority,
                        color = Color.White, // Il testo sul colore di priorità rimane bianco
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getCategoryIcon(recommendation.category),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recommendation.category,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "Impact: ${recommendation.impact}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            // La logica per mostrare/nascondere i pulsanti è già corretta
            if (showActions && recommendation.actionable) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDismiss(recommendation) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Dismiss", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { onAccept(recommendation) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Apply", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private fun getPriorityColor(priority: String): Color {
    return when (priority.lowercase()) {
        "high" -> Color(0xFFF44336)
        "medium" -> Color(0xFFFF9800)
        "low" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "temperature" -> Icons.Default.Thermostat
        "power" -> Icons.Default.BatteryChargingFull
        "apps" -> Icons.Default.Apps
        "charging" -> Icons.Default.Power
        else -> Icons.Default.Lightbulb
    }
}