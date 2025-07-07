package com.example.multi.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openMap(context: Context, address: String) {
    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address))
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
