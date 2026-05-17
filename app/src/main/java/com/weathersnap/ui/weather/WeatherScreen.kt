package com.weathersnap.ui.weather

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.ui.SharedWeatherViewModel
import com.weathersnap.ui.components.WeatherCard
import com.weathersnap.ui.theme.*
import com.weathersnap.utils.WeatherConditionMapper
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(
    onNavigateToCreateReport: () -> Unit,
    onNavigateToSavedReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel(),
    sharedViewModel: SharedWeatherViewModel
) {
    val weatherState   by viewModel.weatherState.collectAsStateWithLifecycle()
    val suggestionState by viewModel.suggestionState.collectAsStateWithLifecycle()
    val reportCount    by viewModel.reportCount.collectAsStateWithLifecycle()
    val focusManager   = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // ── Header ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WeatherSnap", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Refresh — only visible when weather is loaded
                if (weatherState is WeatherUiState.Success) {
                    IconButton(
                        onClick = viewModel::refresh,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface2)
                            .border(1.dp, Border, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Reports badge button
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface2)
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onNavigateToSavedReports
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Reports",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        if (reportCount > 0) {
                            Text(
                                "$reportCount",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Amber
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Search ───────────────────────────────────────────────
        SearchSection(
            suggestionState = suggestionState,
            onQueryChange = viewModel::onQueryChange,
            onCitySelected = { city ->
                viewModel.onCitySelected(city)
                focusManager.clearFocus()
            },
            onDismiss = viewModel::dismissSuggestions
        )

        Spacer(Modifier.height(24.dp))

        // ── Content ──────────────────────────────────────────────
        AnimatedContent(
            targetState = weatherState,
            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
            label = "weather_content"
        ) { state ->
            when (state) {
                is WeatherUiState.Idle    -> IdleState(reportCount)
                is WeatherUiState.Loading -> LoadingState()
                is WeatherUiState.Success -> SuccessState(
                    snapshot = state.snapshot,
                    onCreateReport = {
                        sharedViewModel.setWeather(state.snapshot)
                        onNavigateToCreateReport()
                    },
                    onViewReports = onNavigateToSavedReports
                )
                is WeatherUiState.Error   -> ErrorState(state.message)
            }
        }
    }
}

// ── Search section ────────────────────────────────────────────────

@Composable
private fun SearchSection(
    suggestionState: SuggestionState,
    onQueryChange: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onDismiss: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column {
        OutlinedTextField(
            value = suggestionState.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) onDismiss() },
            placeholder = {
                Text("Search city...", color = TextTertiary, style = MaterialTheme.typography.bodyLarge)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                when {
                    suggestionState.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Amber
                    )
                    suggestionState.query.isNotEmpty() -> IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            supportingText = {
                if (suggestionState.query.length in 1..2) {
                    Text("Type at least 3 characters", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onDismiss() }),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = Surface1,
                unfocusedContainerColor = Surface1,
                focusedBorderColor      = BorderLight,
                unfocusedBorderColor    = Border,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                cursorColor             = Amber
            )
        )

        AnimatedVisibility(
            visible = suggestionState.isExpanded && suggestionState.suggestions.isNotEmpty(),
            enter = expandVertically(tween(180)) + fadeIn(tween(180)),
            exit  = shrinkVertically(tween(140)) + fadeOut(tween(140))
        ) {
            val dropShape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(dropShape)
                    .background(Surface1)
                    .border(1.dp, Border, dropShape)
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(suggestionState.suggestions, key = { it.id }) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    city.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary
                                )
                                Text(city.displayName, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                        }
                        HorizontalDivider(color = Border, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// ── Idle state ────────────────────────────────────────────────────

@Composable
private fun IdleState(reportCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Search a city to see live weather",
            style = MaterialTheme.typography.bodyLarge,
            color = TextTertiary
        )

        if (reportCount > 0) {
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface1)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "$reportCount saved ${if (reportCount == 1) "report" else "reports"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

// ── Loading state ─────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Amber, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
    }
}

// ── Success state ─────────────────────────────────────────────────

@Composable
private fun SuccessState(
    snapshot: WeatherSnapshot,
    onCreateReport: () -> Unit,
    onViewReports: () -> Unit
) {
    // Derived values — all from existing snapshot fields, no new API calls
    val feelsLike   = WeatherConditionMapper.feelsLike(snapshot.temperature, snapshot.humidity)
    val visibility  = WeatherConditionMapper.visibility(snapshot.weatherCode)
    val windDesc    = WeatherConditionMapper.windDescription(snapshot.windSpeed)
    val suggestion  = WeatherConditionMapper.suggestion(snapshot.temperature, snapshot.weatherCode)

    // Comfort index 0–100: lower humidity + moderate temp = more comfortable
    val comfortScore = run {
        val tempScore    = (1.0 - (Math.abs(snapshot.temperature - 22.0) / 30.0).coerceIn(0.0, 1.0)) * 50.0
        val humidScore   = (1.0 - ((snapshot.humidity - 40).coerceAtLeast(0) / 60.0).coerceIn(0.0, 1.0)) * 50.0
        (tempScore + humidScore).roundToInt().coerceIn(0, 100)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Main weather card
        item { WeatherCard(snapshot = snapshot) }

        // Feels like + visibility row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Thermostat,
                    label = "Feels like",
                    value = "${"%.0f".format(feelsLike)}°C"
                )
                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Visibility,
                    label = "Visibility",
                    value = visibility
                )
            }
        }

        // Wind + UV row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Air,
                    label = "Wind",
                    value = windDesc
                )
                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WbSunny,
                    label = "UV Risk",
                    value = uvRisk(snapshot.weatherCode, snapshot.temperature)
                )
            }
        }

        // Comfort index
        item { ComfortIndexCard(score = comfortScore) }

        // Suggestion banner
        item { SuggestionBanner(text = suggestion) }

        // Action buttons
        item {
            Button(
                onClick = onCreateReport,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Background)
            ) {
                Text("Create Report", style = MaterialTheme.typography.labelLarge)
            }
        }
        item {
            OutlinedButton(
                onClick = onViewReports,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("View Reports", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Reusable tiles ────────────────────────────────────────────────

@Composable
private fun InfoTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@Composable
private fun ComfortIndexCard(score: Int) {
    val color = when {
        score >= 70 -> Color(0xFF4CAF50)
        score >= 40 -> Amber
        else        -> ErrorColor
    }
    val label = when {
        score >= 70 -> "Comfortable"
        score >= 40 -> "Moderate"
        else        -> "Uncomfortable"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Comfort Index", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color
            )
        }
        Spacer(Modifier.height(12.dp))
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Surface3)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "$score / 100",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun SuggestionBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmberDim)
            .border(1.dp, AmberBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Lightbulb,
            contentDescription = null,
            tint = Amber,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

// ── Error state ───────────────────────────────────────────────────

@Composable
private fun ErrorState(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ErrorDim)
            .border(1.dp, ErrorColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = ErrorColor)
    }
}

// ── Helpers ───────────────────────────────────────────────────────

private fun uvRisk(code: Int, temp: Double): String = when {
    code in listOf(95, 96, 99, 61, 63, 65, 80, 81, 82, 71, 73, 75, 45, 48) -> "Low"
    code == 3 -> "Low"
    code == 2 -> if (temp > 25) "Moderate" else "Low"
    code in listOf(0, 1) -> when {
        temp >= 35 -> "Very High"
        temp >= 28 -> "High"
        temp >= 20 -> "Moderate"
        else       -> "Low"
    }
    else -> "Low"
}
