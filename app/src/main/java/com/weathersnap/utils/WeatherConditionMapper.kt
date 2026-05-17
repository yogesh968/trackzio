package com.weathersnap.utils

object WeatherConditionMapper {
    fun fromCode(code: Int): String = when (code) {
        0 -> "Clear Sky"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with Hail"
        else -> "Unknown"
    }

    fun toEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
        71, 73, 75 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }

    /** Simple heat-index "feels like" using Steadman approximation (°C). */
    fun feelsLike(tempC: Double, humidity: Int): Double {
        // Below 27°C just use temp adjusted by humidity
        if (tempC < 27) return tempC - (humidity - 40) * 0.05
        val T = tempC
        val R = humidity.toDouble()
        val hi = -8.78469475556 +
                1.61139411 * T +
                2.33854883889 * R +
                -0.14611605 * T * R +
                -0.012308094 * T * T +
                -0.0164248277778 * R * R +
                0.002211732 * T * T * R +
                0.00072546 * T * R * R +
                -0.000003582 * T * T * R * R
        return hi
    }

    /** Visibility description derived from weather code. */
    fun visibility(code: Int): String = when (code) {
        0, 1 -> "Excellent"
        2 -> "Good"
        3 -> "Moderate"
        45, 48 -> "Poor — Fog"
        51, 53, 55 -> "Reduced — Drizzle"
        61, 63, 65, 80, 81, 82 -> "Reduced — Rain"
        71, 73, 75 -> "Reduced — Snow"
        95, 96, 99 -> "Poor — Storm"
        else -> "Good"
    }

    /** Wind description from speed in km/h. */
    fun windDescription(kmh: Double): String = when {
        kmh < 1   -> "Calm"
        kmh < 20  -> "Light breeze"
        kmh < 40  -> "Moderate wind"
        kmh < 60  -> "Strong wind"
        kmh < 90  -> "Near gale"
        else      -> "Storm force"
    }

    /** One-line clothing/activity suggestion. */
    fun suggestion(tempC: Double, code: Int): String = when {
        code in listOf(95, 96, 99)          -> "Stay indoors — thunderstorm"
        code in listOf(71, 73, 75)          -> "Dress warm, expect snow"
        code in listOf(61, 63, 65, 80, 81, 82) -> "Bring an umbrella"
        code in listOf(51, 53, 55)          -> "Light rain jacket recommended"
        code in listOf(45, 48)              -> "Drive carefully — foggy"
        tempC >= 35                         -> "Very hot — stay hydrated"
        tempC >= 28                         -> "Warm day — light clothing"
        tempC >= 18                         -> "Comfortable — enjoy outdoors"
        tempC >= 10                         -> "Cool — bring a light jacket"
        tempC >= 0                          -> "Cold — dress in layers"
        else                                -> "Freezing — heavy coat needed"
    }
}
