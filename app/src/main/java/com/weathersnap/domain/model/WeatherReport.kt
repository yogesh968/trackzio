package com.weathersnap.domain.model

data class WeatherReport(
    val id: Long = 0,
    val weather: WeatherSnapshot,
    val notes: String,
    val imagePath: String?,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val timestamp: Long
)
