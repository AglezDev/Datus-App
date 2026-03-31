package datus.app.com.services

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class NautaAuthService @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://secure.etecsa.net:8443"
    }

    private var savedAttributeUuid: String = ""
    private var savedCsrfHw: String = ""
    private var savedWlanUserIp: String = ""
    private var savedUsername: String = ""

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val initialResponse = httpClient.get("$BASE_URL/")
                val initialHtml = initialResponse.bodyAsText()
                val initialCookies = initialResponse.headers["Set-Cookie"]

                val sessionParams = extractSessionParams(initialHtml)

                val loginResponse = httpClient.submitForm(
                    url = "$BASE_URL/Login",
                    formParameters = ParametersBuilder().apply {
                        append("username", username)
                        append("password", password)
                    }.build()
                ) {
                    headers {
                        initialCookies?.let { append("Cookie", it) }
                        append("Content-Type", "application/x-www-form-urlencoded")
                        append("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36")
                        append("Accept", "text/html,application/xhtml+xml")
                        append("Referer", "$BASE_URL/")
                    }
                }

                val responseBody = loginResponse.bodyAsText()
                val sessionCookies = loginResponse.headers["Set-Cookie"]

                if (loginResponse.status.isSuccess() || responseBody.contains("success", ignoreCase = true)) {
                    val loginSessionParams = extractSessionParams(responseBody)
                    
                    savedAttributeUuid = loginSessionParams.attributeUuid
                    savedCsrfHw = loginSessionParams.csrfHw
                    savedWlanUserIp = loginSessionParams.wlanUserIp
                    savedUsername = username

                    val accountInfo = getAccountInfo(username)

                    Result.success(LoginResponse(
                        success = true,
                        message = "Sesión iniciada correctamente",
                        sessionCookies = sessionCookies ?: "",
                        responseBody = responseBody,
                        username = username,
                        timeUsed = accountInfo?.timeUsed,
                        timeAvailable = accountInfo?.timeAvailable
                    ))
                } else {
                    Result.failure(AuthException(parseErrorMessage(responseBody)))
                }
            } catch (e: Exception) {
                Result.failure(AuthException("Error de conexión: ${e.message}"))
            }
        }
    }

    private data class SessionParams(
        val attributeUuid: String = "",
        val csrfHw: String = "",
        val wlanUserIp: String = ""
    )

    private fun extractSessionParams(html: String): SessionParams {
        var attributeUuid = ""
        var csrfHw = ""
        var wlanUserIp = ""

        val attributeUuidRegex = "ATTRIBUTE_UUID=([^&\"]+)".toRegex()
        val csrfHwRegex = "CSRFHW=([^&\"]+)".toRegex()
        val wlanUserIpRegex = "wlanuserip=([^&\"]+)".toRegex()

        attributeUuidRegex.find(html)?.let { attributeUuid = it.groupValues[1] }
        csrfHwRegex.find(html)?.let { csrfHw = it.groupValues[1] }
        wlanUserIpRegex.find(html)?.let { wlanUserIp = it.groupValues[1] }

        return SessionParams(attributeUuid, csrfHw, wlanUserIp)
    }

    suspend fun getAccountInfo(username: String): AccountInfo? {
        return withContext(Dispatchers.IO) {
            try {
                if (savedAttributeUuid.isEmpty() || savedCsrfHw.isEmpty()) {
                    return@withContext null
                }

                val response = httpClient.submitForm(
                    url = "$BASE_URL/EtecsaQueryServlet",
                    formParameters = ParametersBuilder().apply {
                        append("op", "getLeftTime")
                        append("ATTRIBUTE_UUID", savedAttributeUuid)
                        append("CSRFHW", savedCsrfHw)
                        append("wlanuserip", savedWlanUserIp)
                        append("username", username)
                    }.build()
                ) {
                    headers {
                        append("Content-Type", "application/x-www-form-urlencoded")
                        append("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36")
                        append("Accept", "*/*")
                        append("Referer", "$BASE_URL/")
                    }
                }

                val timeLeft = response.bodyAsText().trim()

                val timeLeftSeconds = parseTimeToSeconds(timeLeft)
                val totalSeconds = 3 * 60 * 60 // Asumiendo 3 horas como ejemplo
                val timeUsedSeconds = if (timeLeftSeconds > 0) totalSeconds - timeLeftSeconds else 0

                val timeUsed = formatSecondsToTime(timeUsedSeconds)
                val timeAvailable = timeLeft

                AccountInfo(
                    username = username,
                    timeUsed = timeUsed,
                    timeAvailable = timeAvailable
                )
            } catch (e: Exception) {
                AccountInfo(
                    username = username,
                    timeUsed = "00:00:00",
                    timeAvailable = "00:00:00"
                )
            }
        }
    }

    private fun parseTimeToSeconds(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toIntOrNull() ?: 0
                hours * 3600 + minutes * 60 + seconds
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun formatSecondsToTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    suspend fun refreshAccountInfo(): AccountInfo? {
        return getAccountInfo(savedUsername)
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                httpClient.get("$BASE_URL/Logout")
                savedAttributeUuid = ""
                savedCsrfHw = ""
                savedWlanUserIp = ""
                savedUsername = ""
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseErrorMessage(responseBody: String): String {
        return when {
            responseBody.contains("incorrecta", ignoreCase = true) -> 
                "Usuario o contraseña incorrectos"
            responseBody.contains("bloqueada", ignoreCase = true) -> 
                "Cuenta bloqueada. Contacte a ETECSA"
            responseBody.contains("expirada", ignoreCase = true) -> 
                "Contraseña expirada. Debe cambiarla en el portal"
            responseBody.contains("saldo", ignoreCase = true) || 
            responseBody.contains("crédito", ignoreCase = true) -> 
                "Sin saldo disponible. Recargue su cuenta"
            responseBody.contains("timeout", ignoreCase = true) ||
            responseBody.contains("tiempo", ignoreCase = true) -> 
                "Tiempo de sesión agotado"
            else -> "Error de autenticación. Verifique sus credenciales"
        }
    }
}

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val sessionCookies: String,
    val responseBody: String = "",
    val username: String = "",
    val timeUsed: String? = null,
    val timeAvailable: String? = null
)

data class AccountInfo(
    val username: String,
    val timeUsed: String,
    val timeAvailable: String
)

class AuthException(message: String) : Exception(message)
