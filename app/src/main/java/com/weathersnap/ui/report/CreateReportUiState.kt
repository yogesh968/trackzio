package com.weathersnap.ui.report

import com.weathersnap.domain.model.WeatherSnapshot

data class CreateReportUiState(
    val weather: WeatherSnapshot? = null,
    val notes: String = "",
    val imagePath: String? = null,
    val originalSizeBytes: Long = 0L,
    val compressedSizeBytes: Long = 0L,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
