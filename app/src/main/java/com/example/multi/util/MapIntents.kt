package com.example.multi.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens Google Maps (or a browser fallback) for the supplied address.
 */
fun openAddressInMaps(context: Context, address: String?) {
    val trimmedAddress = address?.trim().orEmpty()
    val hasAddress = trimmedAddress.isNotEmpty()

    val geoUri = if (hasAddress) {
        Uri.parse("geo:0,0?q=" + Uri.encode(trimmedAddress))
    } else {
        Uri.parse("geo:0,0?q=")
    }

    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        // Prefer the Google Maps app when it is installed.
        setPackage("com.google.android.apps.maps")
    }

    val packageManager = context.packageManager
    if (mapsIntent.resolveActivity(packageManager) != null) {
        context.startActivity(mapsIntent)
        return
    }

    val browserUri = if (hasAddress) {
        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(trimmedAddress))
    } else {
        Uri.parse("https://www.google.com/maps")
    }
    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
    context.startActivity(browserIntent)
}
