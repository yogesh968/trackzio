package com.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.repository.CityRepository
import com.weathersnap.data.repository.ReportRepository
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Result
import com.weathersnap.domain.model.WeatherSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val weatherRepository: WeatherRepository,
    reportRepository: ReportRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weatherState = _weatherState.asStateFlow()

    private val _suggestionState = MutableStateFlow(SuggestionState())
    val suggestionState = _suggestionState.asStateFlow()

    // Last selected city — needed for refresh
    private var lastCity: City? = null

    // Live count of saved reports
    val reportCount: StateFlow<Int> = reportRepository.getAllReports()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val queryFlow = MutableStateFlow("")

    init { observeQueryDebounced() }

    @OptIn(FlowPreview::class)
    private fun observeQueryDebounced() {
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .filter { it.length > 2 }
                .distinctUntilChanged()
                .collect { query -> fetchSuggestions(query) }
        }
    }

    fun onQueryChange(query: String) {
        _suggestionState.update { it.copy(query = query, isExpanded = query.length > 2) }
        queryFlow.value = query
        if (query.length <= 2) {
            _suggestionState.update { it.copy(suggestions = emptyList(), isExpanded = false) }
        }
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            _suggestionState.update { it.copy(isLoading = true) }
            when (val result = cityRepository.searchCities(query)) {
                is Result.Success -> _suggestionState.update {
                    it.copy(suggestions = result.data, isLoading = false, isExpanded = true)
                }
                is Result.Error -> _suggestionState.update {
                    it.copy(suggestions = emptyList(), isLoading = false)
                }
                else -> Unit
            }
        }
    }

    fun onCitySelected(city: City) {
        lastCity = city
        _suggestionState.update {
            it.copy(query = city.displayName, isExpanded = false, suggestions = emptyList())
        }
        fetchWeather(city)
    }

    fun refresh() {
        lastCity?.let { fetchWeather(it) }
    }

    private fun fetchWeather(city: City) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            when (val result = weatherRepository.getWeather(city.name, city.latitude, city.longitude)) {
                is Result.Success -> _weatherState.value = WeatherUiState.Success(result.data)
                is Result.Error   -> _weatherState.value = WeatherUiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun dismissSuggestions() {
        _suggestionState.update { it.copy(isExpanded = false) }
    }

    fun getCurrentSnapshot(): WeatherSnapshot? =
        (_weatherState.value as? WeatherUiState.Success)?.snapshot
}
