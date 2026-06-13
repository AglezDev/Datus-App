package datus.app.com.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class NautaLoginResponse(
    val message: String,
    val username: String,
    val timeUsed: String?,
    val timeAvailable: String?
)

@Singleton
class NautaAuthService @Inject constructor() {

    companion object {
        private const val TAG = "NautaAuthService"
        private const val BASE_URL = "https://secure.etecsa.net:8443"
        private const val LOGIN_URL = "$BASE_URL/LoginServlet"
        private const val LOGOUT_URL = "$BASE_URL/LogoutServlet"
        private const val QUERY_URL = "$BASE_URL/EtecsaQueryServlet"
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }

    private var currentSession: NautaSession? = null
    private var lastTimeUsed: String? = null

    init {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        CookieHandler.setDefault(cookieManager)
    }

    data class NautaSession(
        val username: String,
        val csrfHw: String = "",
        val attributeUuid: String = "",
        val wlanUserIp: String = "",
        val ssid: String = "nauta_hogar",
        val loggerId: String = "",
        val wlanAcName: String = "",
        val wlanMac: String = ""
    )

    private val trustAllSslFactory by lazy {
        val trustAllManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustAllManager), SecureRandom())
        sslContext.socketFactory
    }

    private val hostnameVerifier = HostnameVerifier { _, _ -> true }

    suspend fun login(username: String, password: String): Result<NautaLoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                doGet(BASE_URL)
                val params = mapOf(
                    "username" to username,
                    "password" to password,
                    "CSRFHW" to "",
                    "wlanuserip" to "",
                    "wlanacname" to "",
                    "wlanmac" to ""
                )
                val responseBody = doPost(LOGIN_URL, params)
                if (hasLoginError(responseBody)) {
                    val errorMsg = extractErrorMessage(responseBody)
                    return@withContext Result.failure(NautaAuthException(errorMsg))
                }
                val session = parseSessionInfo(responseBody, username)
                currentSession = session
                val (timeUsed, timeAvailable) = parseTimeFromResponse(responseBody)
                lastTimeUsed = timeUsed

                Result.success(NautaLoginResponse(
                    message = "Inicio de sesión exitoso",
                    username = username,
                    timeUsed = timeUsed,
                    timeAvailable = timeAvailable
                ))
            } catch (e: Exception) {
                Log.e(TAG, "Login failed", e)
                val msg = when {
                    e.message?.contains("Unable to resolve host") == true -> "No hay conexión a internet. Conéctese a una red ETECSA."
                    e.message?.contains("timeout") == true -> "Tiempo de espera agotado. Verifique su conexión."
                    e.message?.contains("Connection refused") == true -> "No está conectado a una red ETECSA."
                    else -> "Error de conexión: ${e.message}"
                }
                Result.failure(NautaAuthException(msg))
            }
        }
    }

    suspend fun refreshAccountInfo(): Result<NautaLoginResponse>? {
        val session = currentSession ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val params = mapOf(
                    "op" to "getLeftTime",
                    "ATTRIBUTE_UUID" to session.attributeUuid,
                    "CSRFHW" to session.csrfHw,
                    "wlanuserip" to session.wlanUserIp,
                    "ssid" to session.ssid,
                    "loggerId" to session.loggerId,
                    "domain" to "",
                    "username" to session.username,
                    "wlanacname" to session.wlanAcName,
                    "wlanmac" to session.wlanMac
                )
                val responseBody = doPost(QUERY_URL, params).trim()
                if (responseBody.contains("errorop", ignoreCase = true)) {
                    Result.failure(NautaAuthException("Error al consultar tiempo"))
                } else {
                    Result.success(NautaLoginResponse(
                        message = "Tiempo actualizado",
                        username = session.username,
                        timeUsed = lastTimeUsed,
                        timeAvailable = responseBody
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Refresh failed", e)
                Result.failure(NautaAuthException("Error al consultar tiempo: ${e.message}"))
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                val session = currentSession ?: return@withContext
                val params = mapOf(
                    "ATTRIBUTE_UUID" to session.attributeUuid,
                    "CSRFHW" to session.csrfHw,
                    "wlanuserip" to session.wlanUserIp,
                    "ssid" to session.ssid,
                    "loggerId" to session.loggerId,
                    "domain" to "",
                    "username" to session.username,
                    "wlanacname" to "",
                    "wlanmac" to "",
                    "remove" to "1"
                )
                try {
                    doPost(LOGOUT_URL, params)
                } catch (_: Exception) {
                    try {
                        doGet(LOGOUT_URL)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
            } finally {
                currentSession = null
                lastTimeUsed = null
            }
        }
    }

    fun isSessionActive(): Boolean = currentSession != null

    private fun doPost(url: String, params: Map<String, String>): String {
        val connection = URL(url).openConnection() as HttpsURLConnection
        try {
            connection.apply {
                requestMethod = "POST"
                sslSocketFactory = trustAllSslFactory
                hostnameVerifier = this@NautaAuthService.hostnameVerifier
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Referer", BASE_URL)
                setRequestProperty("Origin", BASE_URL)
                doInput = true
                doOutput = true
                connectTimeout = 45_000
                readTimeout = 30_000
                instanceFollowRedirects = true
            }
            val body = params.entries.joinToString("&") { (key, value) ->
                "${key}=${java.net.URLEncoder.encode(value, "UTF-8")}"
            }
            connection.outputStream.use { os ->
                os.write(body.toByteArray())
                os.flush()
            }
            return readResponse(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun doGet(url: String): String {
        val connection = URL(url).openConnection() as HttpsURLConnection
        try {
            connection.apply {
                requestMethod = "GET"
                sslSocketFactory = trustAllSslFactory
                hostnameVerifier = this@NautaAuthService.hostnameVerifier
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                doInput = true
                connectTimeout = 30_000
                readTimeout = 30_000
            }
            return readResponse(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpsURLConnection): String {
        return try {
            connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).readText()
            }
        } catch (e: Exception) {
            connection.errorStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).readText()
            } ?: ""
        }
    }

    private fun hasLoginError(html: String): Boolean {
        if (html.contains("loginSuccess = true", ignoreCase = true)) return false
        if (html.contains("success", ignoreCase = true) &&
            html.contains("ATTRIBUTE_UUID") &&
            html.contains("CSRFHW")) return false
        val errorPatterns = listOf(
            "alert(", "Usuario o Contraseña", "incorrecta", "password no valida",
            "bloqueada", "bloqueado", "expirada", "caducada",
            "saldo insuficiente", "no tiene credito",
            "Lamentablemente", "no puede navegar"
        )
        return errorPatterns.any { html.contains(it, ignoreCase = true) }
    }

    private fun extractErrorMessage(html: String): String {
        val alertRegex = """alert\(['"]([^'"]+)['"]\)""".toRegex()
        alertRegex.find(html)?.let {
            return it.groupValues[1]
        }
        val errorRegex = """(?:Error|error|ERROR)\s*[:]\s*([^<.]+)""".toRegex()
        errorRegex.find(html)?.let {
            return it.groupValues[1].trim()
        }
        return "Usuario o contraseña incorrectos"
    }

    private fun parseSessionInfo(html: String, username: String): NautaSession {
        val csrfHw = extractValue(html, """CSRF[Hh][Ww]\s*[=:]\s*['"]?([a-fA-F0-9]+)['"]?""")
        val attributeUuid = extractValue(html, """ATTRIBUTE_UUID\s*[=:]\s*['"]?([a-fA-F0-9\-]+)['"]?""")
        val wlanUserIp = extractValue(html, """wlanuserip\s*[=:]\s*['"]?(\d+\.\d+\.\d+\.\d+)['"]?""")
        val ssid = extractValue(html, """ssid\s*[=:]\s*['"]?([^'\"&]+)['"]?""") ?: "nauta_hogar"
        val loggerId = extractValue(html, """loggerId\s*[=:]\s*['"]?([^'\"&]+)['"]?""")
            ?: "${timestamp()}+${username.substringBefore("@")}@nautaplus"
        val wlanAcName = extractValue(html, """wlanacname\s*[=:]\s*['"]?([^'\"&]+)['"]?""") ?: ""
        val wlanMac = extractValue(html, """wlanmac\s*[=:]\s*['"]?([^'\"&]+)['"]?""") ?: ""

        return NautaSession(
            username = username,
            csrfHw = csrfHw,
            attributeUuid = attributeUuid,
            wlanUserIp = wlanUserIp,
            ssid = ssid,
            loggerId = loggerId,
            wlanAcName = wlanAcName,
            wlanMac = wlanMac
        )
    }

    private fun parseTimeFromResponse(html: String): Pair<String?, String?> {
        var timeUsed: String? = null
        var timeAvailable: String? = null

        val usedRegex = """(?:Tiempo\s*consumido|Tiempo\s*usado|Time\s*used)\s*[:=]?\s*([\d:]+)""".toRegex(RegexOption.IGNORE_CASE)
        usedRegex.find(html)?.let { timeUsed = it.groupValues[1].trim() }

        val availRegex = """(?:Tiempo\s*disponible|Tiempo\s*restante|Time\s*available|Time\s*left)\s*[:=]?\s*([\d:]+)""".toRegex(RegexOption.IGNORE_CASE)
        availRegex.find(html)?.let { timeAvailable = it.groupValues[1].trim() }

        return Pair(timeUsed, timeAvailable)
    }

    private fun extractValue(html: String, regex: String): String {
        return try {
            Regex(regex, RegexOption.IGNORE_CASE).find(html)?.groupValues?.getOrNull(1) ?: ""
        } catch (_: Exception) { "" }
    }

    private fun timestamp(): String {
        return SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
    }
}

class NautaAuthException(message: String) : Exception(message)
