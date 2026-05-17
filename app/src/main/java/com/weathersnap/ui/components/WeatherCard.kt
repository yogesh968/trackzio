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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface2)
            .border(1.dp, Border, RoundedCornerShape(24.dp))
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 24.dp)
    ) {
        // City name
        Text(
            snapshot.cityName,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(8.dp))

        // Giant temperature + emoji on same baseline
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "${"%.0f".format(snapshot.temperature)}°",
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                Text(
                    WeatherConditionMapper.toEmoji(snapshot.weatherCode),
                    fontSize = 40.sp,
                    lineHeight = 40.sp
                )
            }
        }

        // Condition
        Text(
            snapshot.condition,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = Border)
        Spacer(Modifier.height(20.dp))

        // Three metrics
        Row(modifier = Modifier.fillMaxWidth()) {
            CardMetric(Modifier.weight(1f), "${"%.0f".format(snapshot.windSpeed)}", "km/h", "Wind")
            CardMetric(Modifier.weight(1f), "${snapshot.humidity}", "%", "Humidity")
            CardMetric(Modifier.weight(1f), "${"%.0f".format(snapshot.pressure)}", "hPa", "Pressure")
        }
    }
}

@Composable
private fun CardMetric(modifier: Modifier, value: String, unit: String, label: String) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(Modifier.width(3.dp))
            Text(
                unit,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}
