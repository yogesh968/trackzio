package com.weathersnap.ui

import androidx.lifecycle.ViewModel
import com.weathersnap.domain.model.WeatherSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

// Shared ViewModel scoped to the NavGraph for:
// 1. Passing immutable weather snapshot from Weather -> CreateReport
// 2. Passing captured image file from Camera -> CreateReport
@HiltViewModel
class SharedWeatherViewModel @Inject constructor() : ViewModel() {

    private val _selectedWeather = MutableStateFlow<WeatherSnapshot?>(null)
    val selectedWeather = _selectedWeather.asStateFlow()

    private val _capturedImageFile = MutableStateFlow<File?>(null)
    val capturedImageFile = _capturedImageFile.asStateFlow()

    fun setWeather(snapshot: WeatherSnapshot) {
        _selectedWeather.value = snapshot
    }

    fun setCapturedImage(file: File) {
        _capturedImageFile.value = file
    }

    fun consumeCapturedImage(): File? {
        val file = _capturedImageFile.value
        _capturedImageFile.value = null
        return file
    }
}
