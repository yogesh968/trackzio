package com.weathersnap.domain.model

data class WeatherSnapshot(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val weatherCode: Int
)
