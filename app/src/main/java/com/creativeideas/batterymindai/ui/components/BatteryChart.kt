package com.creativeideas.batterymindai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.creativeideas.batterymindai.data.database.entities.BatteryStatsEntity

@Composable
fun BatteryChart(
    batteryHistory: List<BatteryStatsEntity>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Battery History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (batteryHistory.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val padding = 20.dp.toPx()

                    val chartWidth = width - (padding * 2)
                    val chartHeight = height - (padding * 2)

                    if (batteryHistory.size > 1) {
                        val path = Path()
                        val stepX = chartWidth / (batteryHistory.size - 1)

                        batteryHistory.forEachIndexed { index, stats ->
                            val x = padding + (index * stepX)
                            val y = padding + chartHeight - (stats.batteryLevel / 100f * chartHeight)

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color.Blue,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        // Draw points
                        batteryHistory.forEachIndexed { index, stats ->
                            val x = padding + (index * stepX)
                            val y = padding + chartHeight - (stats.batteryLevel / 100f * chartHeight)

                            drawCircle(
                                color = Color.Blue,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
