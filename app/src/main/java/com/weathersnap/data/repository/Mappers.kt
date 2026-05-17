package com.weathersnap.data.repository

import com.weathersnap.data.local.entity.DraftReportEntity
import com.weathersnap.data.local.entity.ReportEntity
import com.weathersnap.data.remote.dto.GeocodingResult
import com.weathersnap.data.remote.dto.WeatherResponse
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.WeatherReport
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.utils.WeatherConditionMapper

fun GeocodingResult.toCity() = City(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude,
    displayName = buildString {
        append(name)
        admin1?.let { append(", $it") }
        append(", $country")
    }
)

fun WeatherResponse.toSnapshot(cityName: String, lat: Double, lon: Double) = WeatherSnapshot(
    cityName = cityName,
    latitude = lat,
    longitude = lon,
    temperature = current.temperature,
    condition = WeatherConditionMapper.fromCode(current.weatherCode),
    humidity = current.humidity,
    windSpeed = current.windSpeed,
    pressure = current.pressure,
    weatherCode = current.weatherCode
)

fun WeatherSnapshot.toReportEntity(
    notes: String,
    imagePath: String?,
    originalSize: Long,
    compressedSize: Long,
    timestamp: Long
) = ReportEntity(
    cityName = cityName,
    latitude = latitude,
    longitude = longitude,
    temperature = temperature,
    condition = condition,
    humidity = humidity,
    windSpeed = windSpeed,
    pressure = pressure,
    weatherCode = weatherCode,
    notes = notes,
    imagePath = imagePath,
    originalSizeBytes = originalSize,
    compressedSizeBytes = compressedSize,
    timestamp = timestamp
)

fun ReportEntity.toReport() = WeatherReport(
    id = id,
    weather = WeatherSnapshot(
        cityName = cityName,
        latitude = latitude,
        longitude = longitude,
        temperature = temperature,
        condition = condition,
        humidity = humidity,
        windSpeed = windSpeed,
        pressure = pressure,
        weatherCode = weatherCode
    ),
    notes = notes,
    imagePath = imagePath,
    originalSizeBytes = originalSizeBytes,
    compressedSizeBytes = compressedSizeBytes,
    timestamp = timestamp
)

fun WeatherSnapshot.toDraft(
    notes: String,
    imagePath: String?,
    originalSize: Long,
    compressedSize: Long
) = DraftReportEntity(
    cityName = cityName,
    latitude = latitude,
    longitude = longitude,
    temperature = temperature,
    condition = condition,
    humidity = humidity,
    windSpeed = windSpeed,
    pressure = pressure,
    weatherCode = weatherCode,
    notes = notes,
    imagePath = imagePath,
    originalSizeBytes = originalSize,
    compressedSizeBytes = compressedSize
)

fun DraftReportEntity.toSnapshot() = WeatherSnapshot(
    cityName = cityName,
    latitude = latitude,
    longitude = longitude,
    temperature = temperature,
    condition = condition,
    humidity = humidity,
    windSpeed = windSpeed,
    pressure = pressure,
    weatherCode = weatherCode
)
