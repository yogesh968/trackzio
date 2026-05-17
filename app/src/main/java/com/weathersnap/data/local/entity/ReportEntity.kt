package com.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val weatherCode: Int,
    val notes: String,
    val imagePath: String?,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val timestamp: Long
)
