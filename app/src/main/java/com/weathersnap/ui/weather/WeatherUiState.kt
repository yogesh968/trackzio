package com.weathersnap.ui.weather

import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.WeatherSnapshot

sealed class WeatherUiState {
    data object Idle : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(val snapshot: WeatherSnapshot) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

data class SuggestionState(
    val query: String = "",
    val suggestions: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val isExpanded: Boolean = false
)
