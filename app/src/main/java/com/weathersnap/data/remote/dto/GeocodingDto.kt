package com.weathersnap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("results") val results: List<GeocodingResult>?
)

data class GeocodingResult(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("admin1") val admin1: String?
)
