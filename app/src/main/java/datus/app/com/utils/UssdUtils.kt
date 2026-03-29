package datus.app.com.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun dialUssdCode(context: Context, ussdCode: String) {
    val encodedHash = Uri.encode("#")
    val ussdUri = "tel:${ussdCode.replace("#", encodedHash)}"
    val intent = Intent(Intent.ACTION_CALL, Uri.parse(ussdUri))
    context.startActivity(intent)
}
