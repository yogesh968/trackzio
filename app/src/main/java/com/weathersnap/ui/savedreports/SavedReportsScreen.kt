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
import androidx.compose.runtime.*
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
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Surface2,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Clear all reports?",
                    style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            },
            text = {
                Text("This will permanently delete all saved reports.",
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAll(); showConfirm = false }) {
                    Text("Delete All", color = ErrorColor,
                        style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = TextSecondary,
                        style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

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
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface2)
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                    tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text("Reports", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(Modifier.weight(1f))
            if (uiState is SavedReportsUiState.Success) {
                TextButton(onClick = { showConfirm = true }) {
                    Text("Clear All", style = MaterialTheme.typography.labelLarge, color = ErrorColor)
                }
            }
        }

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) },
            label = "reports_content"
        ) { state ->
            when (state) {
                is SavedReportsUiState.Loading -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Amber, strokeWidth = 2.dp,
                        modifier = Modifier.size(26.dp))
                }

                is SavedReportsUiState.Empty -> Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No reports yet",
                            style = MaterialTheme.typography.headlineMedium, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text("Create one from the weather screen",
                            style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    }
                }

                is SavedReportsUiState.Success -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(state.reports, key = { _, r -> r.id }) { _, report ->
                        ReportCard(report)
                    }
                    item { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: WeatherReport) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(20.dp))
    ) {
        // Photo
        report.imagePath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = "Report photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {

            // City row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        report.weather.cityName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        report.weather.condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                // Temperature + emoji
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${"%.0f".format(report.weather.temperature)}°",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = TextPrimary,
                        lineHeight = 42.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        WeatherConditionMapper.toEmoji(report.weather.weatherCode),
                        fontSize = 28.sp,
                        lineHeight = 28.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(16.dp))

            // Metrics
            Row(modifier = Modifier.fillMaxWidth()) {
                ReportMetric(Modifier.weight(1f),
                    "${"%.0f".format(report.weather.windSpeed)}", "km/h", "Wind")
                ReportMetric(Modifier.weight(1f),
                    "${report.weather.humidity}", "%", "Humidity")
                ReportMetric(Modifier.weight(1f),
                    "${"%.0f".format(report.weather.pressure)}", "hPa", "Pressure")
            }

            // Notes
            if (report.notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Border)
                Spacer(Modifier.height(14.dp))
                Text(report.notes,
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }

            Spacer(Modifier.height(14.dp))

            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(report.timestamp.toFormattedDate(),
                    style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                if (report.originalSizeBytes > 0) {
                    Text(
                        "${report.originalSizeBytes.toReadableSize()} → ${report.compressedSizeBytes.toReadableSize()}",
                        style = MaterialTheme.typography.labelSmall, color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportMetric(modifier: Modifier, value: String, unit: String, label: String) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary)
            Spacer(Modifier.width(3.dp))
            Text(unit,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}
