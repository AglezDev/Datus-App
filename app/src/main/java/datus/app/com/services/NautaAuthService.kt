package datus.app.com.services

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject

data class NautaLoginResponse(
    val success: Boolean,
    val message: String,
    val username: String = "",
    val timeUsed: String = "",
    val timeAvailable: String = "",
    val csrfHw: String = "",
    val attributeUuid: String = "",
    val wlanUserIp: String = "",
    val ssid: String = "",
    val loggerId: String = "",
    val wlanAcName: String = "",
    val wlanMac: String = ""
)

data class NautaSession(
    val username: String = "",
    val csrfHw: String = "",
    val attributeUuid: String = "",
    val wlanUserIp: String = "",
    val ssid: String = "nauta_hogar",
    val loggerId: String = "",
    val wlanAcName: String = "",
    val wlanMac: String = ""
) {
    val isValid: Boolean
        get() = csrfHw.isNotEmpty() && attributeUuid.isNotEmpty() && username.isNotEmpty()
}

class NautaAuthService @Inject constructor(
    private val httpClient: HttpClient
) {
    private var currentSession: NautaSession? = null

    companion object {
        private const val BASE_URL = "https://secure.etecsa.net:8443"
        private const val LOGIN_URL = "$BASE_URL/LoginServlet"
        private const val LOGOUT_URL = "$BASE_URL/LogoutServlet"
        private const val QUERY_URL = "$BASE_URL/EtecsaQueryServlet"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
    }

    suspend fun login(username: String, password: String): Result<NautaLoginResponse> = withContext(Dispatchers.IO) {
        try {
            val formData = listOf(
                "username" to username,
                "password" to password,
                "CSRFHW" to "",
                "wlanuserip" to "",
                "wlanacname" to "",
                "wlanmac" to ""
            )

            val response = httpClient.post(LOGIN_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(formData)
                userAgent(USER_AGENT)
                header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                header("Referer", "$BASE_URL/")
                header("Origin", BASE_URL)
            }

            val responseText = response.body<String>()

            // Verificar errores primero
            val error = detectError(responseText)
            if (error != null) {
                Result.failure(Exception(error))
            } else if (responseText.contains("ATTRIBUTE_UUID") && responseText.contains("CSRFHW")) {
                // Login exitoso
                val sessionInfo = extractSessionInfo(responseText, username)
                currentSession = sessionInfo

                val timeInfo = extractTimeInfo(responseText)
                
                Result.success(
                    NautaLoginResponse(
                        success = true,
                        message = "Conexión exitosa",
                        username = username,
                        timeUsed = timeInfo.first,
                        timeAvailable = timeInfo.second,
                        csrfHw = sessionInfo.csrfHw,
                        attributeUuid = sessionInfo.attributeUuid,
                        wlanUserIp = sessionInfo.wlanUserIp,
                        ssid = sessionInfo.ssid,
                        loggerId = sessionInfo.loggerId,
                        wlanAcName = sessionInfo.wlanAcName,
                        wlanMac = sessionInfo.wlanMac
                    )
                )
            } else {
                Result.failure(Exception("Error de conexión. Verifique sus credenciales e intente nuevamente."))
            }
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "Tiempo de espera agotado. Verifique la conexión WiFi Nauta."
                e.message?.contains("connection", ignoreCase = true) == true -> 
                    "No se pudo conectar al portal Nauta. Verifique la red WiFi."
                else -> "Error de conexión: ${e.message}"
            }
            Result.failure(Exception(message))
        }
    }

    private fun detectError(html: String): String? {
        // Verificar alert() con mensaje de error
        val alertPattern = Pattern.compile("alert\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)")
        val alertMatcher = alertPattern.matcher(html)
        if (alertMatcher.find()) {
            return alertMatcher.group(1)
        }

        // Verificar mensajes específicos de error
        when {
            html.contains("Usuario o Contraseña", ignoreCase = true) || 
            html.contains("incorrecta", ignoreCase = true) ||
            html.contains("password no válida", ignoreCase = true) -> 
                return "Usuario o contraseña incorrectos"
            
            html.contains("bloqueada", ignoreCase = true) || 
            html.contains("bloqueado", ignoreCase = true) -> 
                return "Cuenta bloqueada. Contacte a ETECSA."
            
            html.contains("expirada", ignoreCase = true) || 
            html.contains("caducada", ignoreCase = true) -> 
                return "Contraseña expirada. Cámbiela en el portal."
            
            html.contains("saldo insuficiente", ignoreCase = true) || 
            html.contains("no tiene crédito", ignoreCase = true) -> 
                return "Saldo insuficiente. Recargue su cuenta."
            
            html.contains("0:00:00") || html.contains("00:00:00") -> 
                return "No tiene tiempo disponible"
            
            // Verificar si contiene el formulario de login nuevamente (sin loginSuccess)
            !html.contains("loginSuccess") && 
            !html.contains("loginformok") &&
            !html.contains("/user/balance") &&
            !html.contains("EtecsaQueryServlet") -> 
                return "Error de autenticación. Verifique sus credenciales."
        }

        return null
    }

    private fun extractSessionInfo(html: String, username: String): NautaSession {
        val csrfHw = extractPattern(html, """CSRFHW\s*[=:]\s*["']?([a-fA-F0-9]+)["']?""") ?: ""
        val attributeUuid = extractPattern(html, """ATTRIBUTE_UUID\s*[=:]\s*["']?([a-fA-F0-9]+)["']?""") ?: ""
        val wlanUserIp = extractPattern(html, """wlanuserip\s*[=:]\s*["']?(\d+\.\d+\.\d+\.\d+)["']?""") ?: ""
        val ssid = extractPattern(html, """ssid\s*[=:]\s*["']?([^"'&]+)["']?""") ?: "nauta_hogar"
        val wlanAcName = extractPattern(html, """wlanacname\s*[=:]\s*["']?([^"'&]+)["']?""") ?: ""
        val wlanMac = extractPattern(html, """wlanmac\s*[=:]\s*["']?([^"'&]+)["']?""") ?: ""
        
        var loggerId = extractPattern(html, """loggerId\s*[=:]\s*["']?([^"'&]+)["']?""") ?: ""
        if (loggerId.isEmpty()) {
            loggerId = "${System.currentTimeMillis()}+${username}"
        }

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

    private fun extractTimeInfo(html: String): Pair<String, String> {
        // Intentar extraer tiempo usado y disponible del HTML
        val timeUsedPattern = Pattern.compile("""tiempo[^"]*consumido[^"]*["']?(\d+:\d+:\d+)["']?""", Pattern.CASE_INSENSITIVE)
        val timeAvailablePattern = Pattern.compile("""tiempo[^"]*restante[^"]*["']?(\d+:\d+:\d+)["']?""", Pattern.CASE_INSENSITIVE)
        
        val usedMatcher = timeUsedPattern.matcher(html)
        val availableMatcher = timeAvailablePattern.matcher(html)
        
        val timeUsed = if (usedMatcher.find()) usedMatcher.group(1) else "00:00:00"
        val timeAvailable = if (availableMatcher.find()) availableMatcher.group(1) else "00:00:00"
        
        return Pair(timeUsed, timeAvailable)
    }

    private fun extractPattern(html: String, regex: String): String? {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }

    suspend fun getRemainingTime(): Result<String> = withContext(Dispatchers.IO) {
        val session = currentSession
        if (session == null || !session.isValid) {
            return@withContext Result.failure(Exception("No hay sesión activa"))
        }

        try {
            val formData = listOf(
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

            val response = httpClient.post(QUERY_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(formData)
                userAgent(USER_AGENT)
                header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                header("Referer", "$BASE_URL/")
            }

            val responseText = response.body<String>()
            
            if (responseText.contains("errorop", ignoreCase = true)) {
                Result.failure(Exception("Error al consultar tiempo restante"))
            } else {
                Result.success(responseText.trim())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun refreshAccountInfo(): Result<NautaLoginResponse>? {
        val session = currentSession ?: return null
        
        val timeResult = getRemainingTime()
        return if (timeResult.isSuccess) {
            Result.success(
                NautaLoginResponse(
                    success = true,
                    message = "Información actualizada",
                    username = session.username,
                    timeUsed = "00:00:00", // Se puede mejorar extrayendo del HTML
                    timeAvailable = timeResult.getOrNull() ?: "00:00:00",
                    csrfHw = session.csrfHw,
                    attributeUuid = session.attributeUuid,
                    wlanUserIp = session.wlanUserIp,
                    ssid = session.ssid,
                    loggerId = session.loggerId,
                    wlanAcName = session.wlanAcName,
                    wlanMac = session.wlanMac
                )
            )
        } else {
            Result.failure(timeResult.exceptionOrNull() ?: Exception("Error desconocido"))
        }
    }

    suspend fun logout(): Result<String> = withContext(Dispatchers.IO) {
        val session = currentSession
        if (session == null) {
            return@withContext Result.failure(Exception("No hay sesión activa"))
        }

        try {
            // Intentar POST primero
            val formData = listOf(
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

            val response = httpClient.post(LOGOUT_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(formData)
                userAgent(USER_AGENT)
                header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                header("Referer", "$BASE_URL/")
            }

            val responseText = response.body<String>()
            
            if (responseText.contains("SUCCESS", ignoreCase = true) || 
                responseText.contains("REMOVE_AUTHINFO_SUCCESS", ignoreCase = true)) {
                currentSession = null
                return@withContext Result.success("Sesión cerrada correctamente")
            }

            // Fallback a GET
            val getResponse = httpClient.get(LOGOUT_URL) {
                userAgent(USER_AGENT)
                header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }

            currentSession = null
            Result.success("Sesión cerrada correctamente")
        } catch (e: Exception) {
            currentSession = null
            Result.failure(Exception("Error al cerrar sesión: ${e.message}"))
        }
    }

    fun getCurrentSession(): NautaSession? = currentSession
}
