package com.weathersnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.ui.theme.*
import com.weathersnap.utils.WeatherConditionMapper

@Composable
fun WeatherCard(snapshot: WeatherSnapshot, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface2)
            .border(1.dp, Border, shape)
            .padding(24.dp)
    ) {
        // Location
        Text(
            snapshot.cityName,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(16.dp))

        // Temperature + emoji side by side
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${"%.0f".format(snapshot.temperature)}°",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp,
                    color = TextPrimary,
                    lineHeight = 64.sp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    WeatherConditionMapper.toEmoji(snapshot.weatherCode),
                    fontSize = 36.sp,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            snapshot.condition,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = Border, thickness = 1.dp)

        Spacer(Modifier.height(20.dp))

        // Metrics
        Row(modifier = Modifier.fillMaxWidth()) {
            MetricItem(
                modifier = Modifier.weight(1f),
                label = "Humidity",
                value = "${snapshot.humidity}%"
            )
            MetricItem(
                modifier = Modifier.weight(1f),
                label = "Wind",
                value = "${"%.0f".format(snapshot.windSpeed)} km/h"
            )
            MetricItem(
                modifier = Modifier.weight(1f),
                label = "Pressure",
                value = "${"%.0f".format(snapshot.pressure)} hPa"
            )
        }
    }
}

@Composable
private fun MetricItem(modifier: Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}
