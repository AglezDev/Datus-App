package datus.app.com.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

/**
 * Verifica si el dispositivo está conectado a una red WiFi con portal cautivo de ETECSA (Nauta Hogar)
 */
fun isConnectedToNautaWifi(context: Context): Boolean {
    val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    // Verificar si está conectado a WiFi
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    
    if (networkCapabilities == null || !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        return false
    }
    
    // Obtener el SSID de la red WiFi actual
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ssid = wifiInfo.ssid ?: return false
    
    // Las redes ETECSA/Nauta generalmente tienen SSIDs que contienen "ETECSA" o "Nauta"
    val nautaIdentifiers = listOf(
        "ETECSA",
        "NAUTA",
        "nauta",
        "etecsa",
        "Nauta Hogar",
        "WiFi ETECSA"
    )
    
    // Verificar si el SSID coincide con alguna red ETECSA/Nauta
    val isNautaNetwork = nautaIdentifiers.any { ssid.contains(it, ignoreCase = true) }
    
    // También verificamos si la red tiene portal cautivo
    val hasCaptivePortal = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
    
    // Retornamos true si es una red ETECSA/Nauta O si tiene portal cautivo
    return isNautaNetwork || hasCaptivePortal
}

/**
 * Verifica si WiFi está conectado (independientemente de la red)
 */
fun isWifiConnected(context: Context): Boolean {
    val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    
    return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
}
