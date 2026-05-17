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
    val weatherState    by viewModel.weatherState.collectAsStateWithLifecycle()
    val suggestionState by viewModel.suggestionState.collectAsStateWithLifecycle()
    val reportCount     by viewModel.reportCount.collectAsStateWithLifecycle()
    val focusManager    = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        // ── Top bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "WeatherSnap",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (weatherState is WeatherUiState.Success) {
                    TopBarButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh",
                            tint = TextSecondary, modifier = Modifier.size(17.dp))
                    }
                }
                TopBarButton(onClick = onNavigateToSavedReports) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Reports",
                            tint = TextSecondary, modifier = Modifier.size(17.dp))
                        if (reportCount > 0) {
                            Text(
                                "$reportCount",
                                style = MaterialTheme.typography.labelLarge,
                                color = Amber
                            )
                        }
                    }
                }
            }
        }

        // ── Search ───────────────────────────────────────────────
        SearchSection(
            modifier = Modifier.padding(horizontal = 20.dp),
            suggestionState = suggestionState,
            onQueryChange = viewModel::onQueryChange,
            onCitySelected = { city ->
                viewModel.onCitySelected(city)
                focusManager.clearFocus()
            },
            onDismiss = viewModel::dismissSuggestions
        )

        // ── Content ──────────────────────────────────────────────
        AnimatedContent(
            targetState = weatherState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(180)) },
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

@Composable
private fun TopBarButton(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun SearchSection(
    modifier: Modifier = Modifier,
    suggestionState: SuggestionState,
    onQueryChange: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = modifier) {
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
                Icon(Icons.Default.Search, contentDescription = null,
                    tint = TextTertiary, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                when {
                    suggestionState.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Amber)
                    suggestionState.query.isNotEmpty() -> IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear",
                            tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            supportingText = {
                if (suggestionState.query.length in 1..2) {
                    Text("Type at least 3 characters",
                        style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onDismiss() }),
            shape = RoundedCornerShape(14.dp),
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
            enter = expandVertically(tween(160)) + fadeIn(tween(160)),
            exit  = shrinkVertically(tween(120)) + fadeOut(tween(120))
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
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null,
                                tint = TextTertiary, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(city.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary)
                                Text(city.displayName,
                                    style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                        }
                        HorizontalDivider(color = Border, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleState(reportCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Search a city to see live weather",
            style = MaterialTheme.typography.bodyLarge,
            color = TextTertiary
        )
        if (reportCount > 0) {
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface1)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null,
                    tint = Amber, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "$reportCount saved ${if (reportCount == 1) "report" else "reports"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Amber, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun SuccessState(
    snapshot: WeatherSnapshot,
    onCreateReport: () -> Unit,
    onViewReports: () -> Unit
) {
    val feelsLike    = WeatherConditionMapper.feelsLike(snapshot.temperature, snapshot.humidity)
    val visibility   = WeatherConditionMapper.visibility(snapshot.weatherCode)
    val windDesc     = WeatherConditionMapper.windDescription(snapshot.windSpeed)
    val suggestion   = WeatherConditionMapper.suggestion(snapshot.temperature, snapshot.weatherCode)
    val uv           = uvRisk(snapshot.weatherCode, snapshot.temperature)
    val comfortScore = run {
        val t = (1.0 - (Math.abs(snapshot.temperature - 22.0) / 30.0).coerceIn(0.0, 1.0)) * 50.0
        val h = (1.0 - ((snapshot.humidity - 40).coerceAtLeast(0) / 60.0).coerceIn(0.0, 1.0)) * 50.0
        (t + h).roundToInt().coerceIn(0, 100)
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { WeatherCard(snapshot = snapshot) }

        // 2-column tile row: feels like + visibility
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Thermostat,
                    iconTint = Color(0xFF64B5F6),
                    value = "${"%.0f".format(feelsLike)}°",
                    label = "Feels like"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Visibility,
                    iconTint = Color(0xFF81C784),
                    value = visibility,
                    label = "Visibility"
                )
            }
        }

        // 2-column tile row: wind + UV
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Air,
                    iconTint = Color(0xFF90CAF9),
                    value = windDesc,
                    label = "Wind"
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFFFFD54F),
                    value = uv,
                    label = "UV Risk"
                )
            }
        }

        // Comfort index — full width
        item { ComfortTile(score = comfortScore) }

        // Suggestion — full width
        item { SuggestionTile(text = suggestion) }

        // Spacer between info and actions
        item { Spacer(Modifier.height(4.dp)) }

        // Primary action
        item {
            Button(
                onClick = onCreateReport,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor   = Background
                )
            ) {
                Text("Create Report", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Secondary action
        item {
            OutlinedButton(
                onClick = onViewReports,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("View Reports", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Tiles ─────────────────────────────────────────────────────────

@Composable
private fun StatTile(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null,
                tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(value,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            maxLines = 1
        )
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@Composable
private fun ComfortTile(score: Int) {
    val (barColor, statusText) = when {
        score >= 70 -> GreenColor  to "Comfortable"
        score >= 40 -> Amber       to "Moderate"
        else        -> ErrorColor  to "Uncomfortable"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface1)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Comfort", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
                statusText,
                style = MaterialTheme.typography.labelLarge,
                color = barColor
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Surface3)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(score / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "$score out of 100",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun SuggestionTile(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AmberDim)
            .border(1.dp, AmberBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Lightbulb, contentDescription = null,
            tint = Amber, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
    }
}

@Composable
private fun ErrorState(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ErrorDim)
            .border(1.dp, ErrorColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null,
            tint = ErrorColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = ErrorColor)
    }
}

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
