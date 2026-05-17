package com.weathersnap.domain.model

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val displayName: String = "$name, $country"
)
