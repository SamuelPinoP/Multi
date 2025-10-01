package com.example.multi.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens Google Maps for the provided [address] using a geo URI. If Google Maps is not installed,
 * a browser fallback is attempted instead.
 */
fun Context.openMaps(address: String?) {
    val query = address?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) } ?: ""
    val geoUri = Uri.parse("geo:0,0?q=$query")
    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        startActivity(mapsIntent)
    } catch (exception: ActivityNotFoundException) {
        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(browserIntent)
    }
}
