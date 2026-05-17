package com.weathersnap.ui.savedreports

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.weathersnap.domain.model.WeatherReport
import com.weathersnap.ui.theme.*
import com.weathersnap.utils.WeatherConditionMapper
import com.weathersnap.utils.toFormattedDate
import com.weathersnap.utils.toReadableSize
import java.io.File

@Composable
fun SavedReportsScreen(
    onBack: () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface2)
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text("Saved Reports", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "reports_content"
        ) { state ->
            when (state) {
                is SavedReportsUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Amber, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }

                is SavedReportsUiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No reports yet", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Create one from the weather screen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    }
                }

                is SavedReportsUiState.Success -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(state.reports, key = { _, r -> r.id }) { _, report ->
                        ReportCard(report = report)
                    }
                    item { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: WeatherReport) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Surface1)
            .border(1.dp, Border, shape)
    ) {
        // Photo
        report.imagePath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = "Report photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(18.dp)) {
            // City + temp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    WeatherConditionMapper.toEmoji(report.weather.weatherCode),
                    fontSize = 28.sp
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.weather.cityName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(report.weather.condition, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
                Text(
                    "${"%.0f".format(report.weather.temperature)}°",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Border, thickness = 1.dp)
            Spacer(Modifier.height(14.dp))

            // Metrics
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(modifier = Modifier.weight(1f), value = "${report.weather.humidity}%", label = "Humidity")
                MetricCell(modifier = Modifier.weight(1f), value = "${"%.0f".format(report.weather.windSpeed)} km/h", label = "Wind")
                MetricCell(modifier = Modifier.weight(1f), value = "${"%.0f".format(report.weather.pressure)}", label = "hPa")
            }

            // Notes
            if (report.notes.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = Border, thickness = 1.dp)
                Spacer(Modifier.height(12.dp))
                Text(report.notes, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            // Compression
            if (report.originalSizeBytes > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "${report.originalSizeBytes.toReadableSize()} → ${report.compressedSizeBytes.toReadableSize()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                report.timestamp.toFormattedDate(),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun MetricCell(modifier: Modifier, value: String, label: String) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}
