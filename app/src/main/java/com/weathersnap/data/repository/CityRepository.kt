package com.weathersnap.data.repository

import com.weathersnap.data.remote.api.GeocodingApi
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepository @Inject constructor(
    private val api: GeocodingApi
) {
    // In-memory cache: query -> results
    private val cache = mutableMapOf<String, List<City>>()

    suspend fun searchCities(query: String): Result<List<City>> = withContext(Dispatchers.IO) {
        val key = query.lowercase().trim()
        cache[key]?.let { return@withContext Result.Success(it) }
        try {
            val response = api.searchCities(query)
            val cities = response.results?.map { it.toCity() } ?: emptyList()
            cache[key] = cities
            Result.Success(cities)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch cities", e)
        }
    }
}
