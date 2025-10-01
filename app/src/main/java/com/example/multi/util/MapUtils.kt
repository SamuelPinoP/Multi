package com.example.multi.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

private const val MAPS_PACKAGE = "com.google.android.apps.maps"

/**
 * Launches Google Maps (or a browser fallback) with the provided address.
 *
 * @param address Optional address to search for. If blank, opens the app home.
 */
fun Context.openAddressInMaps(address: String?) {
    val query = address?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) } ?: ""
    val geoUri = Uri.parse("geo:0,0?q=$query")

    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage(MAPS_PACKAGE)
    }

    val unrestrictedIntent = Intent(Intent.ACTION_VIEW, geoUri)
    val browserUri = if (query.isNotEmpty()) {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
    } else {
        Uri.parse("https://www.google.com/maps")
    }

    try {
        startActivity(mapsIntent)
    } catch (primary: ActivityNotFoundException) {
        try {
            startActivity(unrestrictedIntent)
        } catch (secondary: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }
}
