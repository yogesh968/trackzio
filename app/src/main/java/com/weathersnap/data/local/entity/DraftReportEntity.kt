package com.weathersnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Draft table for crash/rotation recovery of in-progress reports
@Entity(tableName = "draft_report")
data class DraftReportEntity(
    @PrimaryKey val id: Int = 1, // singleton draft
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
    val updatedAt: Long = System.currentTimeMillis()
)
