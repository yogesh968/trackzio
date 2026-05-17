package com.weathersnap.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String =
    SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(this))

fun Long.toReadableSize(): String = when {
    this < 1024 -> "$this B"
    this < 1024 * 1024 -> "${"%.1f".format(this / 1024.0)} KB"
    else -> "${"%.1f".format(this / (1024.0 * 1024))} MB"
}
