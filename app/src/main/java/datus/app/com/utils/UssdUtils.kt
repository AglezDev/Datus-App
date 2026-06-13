package datus.app.com.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun dialUssdCode(context: Context, ussdCode: String) {
    try {
        val encodedHash = Uri.encode("#")
        val ussdUri = "tel:${ussdCode.replace("#", encodedHash)}"
        context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse(ussdUri)))
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permiso para llamar denegado", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show()
    }
}
