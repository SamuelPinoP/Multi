package com.example.multi.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openMap(context: Context, address: String) {
    if (address.isBlank()) return
    val uri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}
