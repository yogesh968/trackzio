package com.weathersnap.data.repository

import com.weathersnap.data.remote.api.WeatherApi
import com.weathersnap.domain.model.Result
import com.weathersnap.domain.model.WeatherSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApi
) {
    suspend fun getWeather(cityName: String, lat: Double, lon: Double): Result<WeatherSnapshot> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getWeather(lat, lon)
                Result.Success(response.toSnapshot(cityName, lat, lon))
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to fetch weather", e)
            }
        }
}
