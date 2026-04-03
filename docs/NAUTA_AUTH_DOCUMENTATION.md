# 📡 Documentación: Autenticación Portal Nauta (ETEC SA)

> **Referencia técnica para integración con el portal cautivo Nauta de Cuba**

---

## 🌐 Endpoints del Portal

### Base URL
```
https://secure.etecsa.net:8443/
```

### Endpoints Principales

| Endpoint | Método | Propósito |
|----------|--------|-----------|
| `/` | GET | Página inicial (obtener cookies y tokens de sesión) |
| `/LoginServlet` | POST | Autenticación de usuario |
| `/LogoutServlet` | POST/GET | Cerrar sesión |
| `/EtecsaQueryServlet` | POST | Consultar tiempo restante |

---

## 🔐 Flujo de Autenticación

### Paso 1: Obtener Tokens de Sesión

El portal Nauta utiliza un sistema de tokens CSRF y parámetros de sesión únicos.

**Request:**
```http
GET https://secure.etecsa.net:8443/
Headers:
  User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
  Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
```

**Respuesta:** HTML con parámetros ocultos que incluyen:
- `CSRFHW` - Token CSRF único de la sesión
- `ATTRIBUTE_UUID` - Identificador único de la sesión
- `wlanuserip` - IP del usuario en la red
- `ssid` - Nombre de la red (ej: `nauta_hogar`)
- `loggerId` - Identificador de log
- `wlanacname` - Nombre del access point
- `wlanmac` - MAC del cliente

---

### Paso 2: Enviar Credenciales

**Request:**
```http
POST https://secure.etecsa.net:8443/LoginServlet
Content-Type: application/x-www-form-urlencoded

username=usuario@nauta.com.cu
&password=contraseña
&CSRFHW=<token_obtenido>
&wlanuserip=<ip_obtenida>
&wlanacname=<ac_name_obtenido>
&wlanmac=<mac_obtenido>
```

**Parámetros Requeridos:**
| Parámetro | Requerido | Descripción |
|-----------|-----------|-------------|
| `username` | ✅ | Usuario completo (ej: `usuario@nauta.com.cu`) |
| `password` | ✅ | Contraseña del usuario |
| `CSRFHW` | ⚠️ | Token CSRF (puede enviarse vacío en algunos casos) |
| `wlanuserip` | ⚠️ | IP del usuario (puede enviarse vacío) |
| `wlanacname` | ⚠️ | Nombre del AP (puede enviarse vacío) |
| `wlanmac` | ⚠️ | MAC del cliente (puede enviarse vacío) |

> **Nota:** Los parámetros marcados con ⚠️ pueden enviarse vacíos en el login inicial, pero el portal los devolverá en la respuesta para usarlos en requests posteriores.

---

### Paso 3: Verificar Login Exitoso

**Indicadores de Éxito en la Respuesta:**
- Contiene `ATTRIBUTE_UUID` y `CSRFHW`
- Contiene `loginSuccess = true`
- Contiene `window.location.href = "/success"`
- Contiene `loginformok`
- Contiene `/user/balance`
- Contiene `EtecsaQueryServlet`
- NO contiene indicadores de error (ver abajo)

**Indicadores de Error:**
- `alert(` con mensaje de error
- `Usuario o Contraseña` incorrecta
- `incorrecta` / `password no válida`
- `bloqueada` / `bloqueado`
- `expirada` / `caducada`
- `saldo insuficiente` / `no tiene crédito`
- `0:00:00` o `00:00:00` (tiempo en 0)
- Contiene el formulario de login nuevamente (sin `loginSuccess`)

---

### Paso 4: Extraer Tokens de Sesión

Después del login exitoso, extraer los tokens de la respuesta HTML:

**Regex para extracción:**
```regex
// CSRFHW
CSRFHW\s*[=:]\s*["']?([a-fA-F0-9]+)["']?
CSRFHW=([a-fA-F0-9]+)  // URL param style

// ATTRIBUTE_UUID
ATTRIBUTE_UUID\s*[=:]\s*["']?([a-fA-F0-9]+)["']?
ATTRIBUTE_UUID=([a-fA-F0-9]+)

// wlanuserip
wlanuserip\s*[=:]\s*["']?(\d+\.\d+\.\d+\.\d+)["']?
wlanuserip=(\d+\.\d+\.\d+\.\d+)

// loggerId
loggerId\s*[=:]\s*["']?([^"'&]+)["']?

// ssid
ssid\s*[=:]\s*["']?([^"'&]+)["']?
```

**Formato del loggerId:**
```
{timestamp}{username}@nautaplus
Ej: 20251225195142874+213735511594@nautaplus
```

---

### Paso 5: Consultar Tiempo Restante

**Request:**
```http
POST https://secure.etecsa.net:8443/EtecsaQueryServlet
Content-Type: application/x-www-form-urlencoded

op=getLeftTime
&ATTRIBUTE_UUID=<uuid>
&CSRFHW=<csrf>
&wlanuserip=<ip>
&ssid=<ssid>
&loggerId=<loggerId>
&domain=
&username=<username>
&wlanacname=<acname>
&wlanmac=<mac>
```

**Respuesta Exitosa:**
```
625:51:15
```
(Horas:Minutos:Segundos)

**Respuesta de Error:**
```
errorop
```

---

### Paso 6: Cerrar Sesión (Logout)

**Request:**
```http
POST https://secure.etecsa.net:8443/LogoutServlet
Content-Type: application/x-www-form-urlencoded

ATTRIBUTE_UUID=<uuid>
&CSRFHW=<csrf>
&wlanuserip=<ip>
&ssid=nauta_hogar
&loggerId=<loggerId>
&domain=
&username=<username>
&wlanacname=
&wlanmac=
&remove=1
```

**Indicadores de Éxito:**
- Contiene `SUCCESS`
- Contiene `REMOVE_AUTHINFO_SUCCESS`

**Fallback:** Si POST falla, intentar GET:
```http
GET https://secure.etecsa.net:8443/LogoutServlet
```

---

## 🔧 Configuración HTTP Recomendada

### HttpClient Settings

```csharp
var httpClientHandler = new HttpClientHandler
{
    ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true,
    UseCookies = true,
    CookieContainer = new System.Net.CookieContainer(),
    AllowAutoRedirect = true
};

var httpClient = new HttpClient(httpClientHandler)
{
    Timeout = TimeSpan.FromSeconds(30),
    BaseAddress = new Uri("https://secure.etecsa.net:8443/")
};

httpClient.DefaultRequestHeaders.Add("User-Agent", 
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
```

### Headers Recomendados

| Header | Valor |
|--------|-------|
| `User-Agent` | Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 |
| `Accept` | text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8 |
| `Accept-Language` | es-ES,es;q=0.9,en;q=0.8 |
| `Content-Type` | application/x-www-form-urlencoded |
| `Referer` | https://secure.etecsa.net:8443/ |
| `Origin` | https://secure.etecsa.net:8443 |

---

## 📊 Modelo de Sesión

```csharp
public class NautaSession
{
    public string Username { get; set; } = string.Empty;
    public string CsrfHw { get; set; } = string.Empty;
    public string AttributeUuid { get; set; } = string.Empty;
    public string WlanUserIp { get; set; } = string.Empty;
    public string Ssid { get; set; } = "nauta_hogar";
    public string LoggerId { get; set; } = string.Empty;
    public string WlanAcName { get; set; } = string.Empty;
    public string WlanMac { get; set; } = string.Empty;

    public bool IsValid => 
        !string.IsNullOrEmpty(CsrfHw) && 
        !string.IsNullOrEmpty(AttributeUuid) && 
        !string.IsNullOrEmpty(Username);
}
```

---

## ⚠️ Consideraciones Importantes

### 1. Red WiFi Requerida
El usuario **debe estar conectado** a una red WiFi con portal cautivo ETECSA:
- `nauta_hogar`
- `nauta`
- Otras redes ETECSA

### 2. Manejo de Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `Usuario o Contraseña incorrectos` | Credenciales inválidas | Verificar usuario/contraseña |
| `Cuenta bloqueada` | Múltiples intentos fallidos | Esperar o contactar ETECSA |
| `Contraseña expirada` | Password caducada | Cambiar contraseña en portal |
| `Sin saldo` | Saldo insuficiente | Recargar cuenta |
| `Timeout` / `Conexión rechazada` | No está en red ETECSA | Conectarse a WiFi Nauta |

### 3. Tokens de Sesión
- Los tokens **caducan** cuando el usuario se desconecta
- Los tokens **caducan** por inactividad (tiempo límite)
- Los tokens **caducan** al cambiar de red
- **Siempre** verificar validez antes de usar

### 4. Concurrencia
- **Una sesión por usuario** - múltiples logins desde mismo usuario pueden invalidar sesiones anteriores
- El portal puede limitar sesiones concurrentes

---

## 📝 Ejemplo de Implementación Completa

```csharp
public class NautaAuthService
{
    private readonly HttpClient _httpClient;
    private NautaSession? _currentSession;

    public NautaAuthService()
    {
        _httpClient = new HttpClient(new HttpClientHandler
        {
            ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true,
            UseCookies = true,
            CookieContainer = new System.Net.CookieContainer(),
            AllowAutoRedirect = true
        })
        {
            Timeout = TimeSpan.FromSeconds(30),
            BaseAddress = new Uri("https://secure.etecsa.net:8443/")
        };
        _httpClient.DefaultRequestHeaders.Add("User-Agent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
    }

    public async Task<bool> ConnectAsync(string username, string password)
    {
        var loginUrl = "https://secure.etecsa.net:8443/LoginServlet";
        
        var formData = new Dictionary<string, string>
        {
            ["username"] = username,
            ["password"] = password,
            ["CSRFHW"] = "",
            ["wlanuserip"] = "",
            ["wlanacname"] = "",
            ["wlanmac"] = ""
        };

        var content = new FormUrlEncodedContent(formData);
        var response = await _httpClient.PostAsync(loginUrl, content);
        var responseContent = await response.Content.ReadAsStringAsync();

        if (response.IsSuccessStatusCode && 
            responseContent.Contains("ATTRIBUTE_UUID") && 
            responseContent.Contains("CSRFHW"))
        {
            _currentSession = ExtractSessionInfo(responseContent, username);
            return _currentSession.IsValid;
        }

        return false;
    }

    public async Task<string?> GetRemainingTimeAsync()
    {
        if (_currentSession == null || !_currentSession.IsValid) 
            return "No disponible";

        var formData = new Dictionary<string, string>
        {
            ["op"] = "getLeftTime",
            ["ATTRIBUTE_UUID"] = _currentSession.AttributeUuid,
            ["CSRFHW"] = _currentSession.CsrfHw,
            ["wlanuserip"] = _currentSession.WlanUserIp,
            ["ssid"] = _currentSession.Ssid,
            ["loggerId"] = _currentSession.LoggerId,
            ["domain"] = "",
            ["username"] = _currentSession.Username,
            ["wlanacname"] = _currentSession.WlanAcName,
            ["wlanmac"] = _currentSession.WlanMac
        };

        var content = new FormUrlEncodedContent(formData);
        var response = await _httpClient.PostAsync(
            "https://secure.etecsa.net:8443/EtecsaQueryServlet", 
            content
        );

        if (response.IsSuccessStatusCode)
        {
            var responseText = await response.Content.ReadAsStringAsync();
            return responseText.Contains("errorop") ? "Error" : responseText.Trim();
        }

        return "No disponible";
    }

    public async Task<bool> DisconnectAsync()
    {
        var formData = new Dictionary<string, string>
        {
            ["ATTRIBUTE_UUID"] = _currentSession?.AttributeUuid ?? "",
            ["CSRFHW"] = _currentSession?.CsrfHw ?? "",
            ["wlanuserip"] = _currentSession?.WlanUserIp ?? "",
            ["ssid"] = _currentSession?.Ssid ?? "nauta_hogar",
            ["loggerId"] = _currentSession?.LoggerId ?? "",
            ["domain"] = "",
            ["username"] = _currentSession?.Username ?? "",
            ["wlanacname"] = "",
            ["wlanmac"] = "",
            ["remove"] = "1"
        };

        var content = new FormUrlEncodedContent(formData);
        var response = await _httpClient.PostAsync(
            "https://secure.etecsa.net:8443/LogoutServlet", 
            content
        );

        if (response.IsSuccessStatusCode)
        {
            var responseText = await response.Content.ReadAsStringAsync();
            if (responseText.Contains("SUCCESS") || 
                responseText.Contains("REMOVE_AUTHINFO_SUCCESS"))
            {
                _currentSession = null;
                return true;
            }
        }

        // Fallback a GET
        var getResponse = await _httpClient.GetAsync(
            "https://secure.etecsa.net:8443/LogoutServlet"
        );
        
        return getResponse.IsSuccessStatusCode;
    }

    private NautaSession ExtractSessionInfo(string html, string username)
    {
        var session = new NautaSession { Username = username };

        var matchCsrf = Regex.Match(html, @"CSRFHW\s*[=:]\s*[""']?([a-fA-F0-9]+)[""']?");
        if (matchCsrf.Success) session.CsrfHw = matchCsrf.Groups[1].Value;

        var matchUuid = Regex.Match(html, @"ATTRIBUTE_UUID\s*[=:]\s*[""']?([a-fA-F0-9]+)[""']?");
        if (matchUuid.Success) session.AttributeUuid = matchUuid.Groups[1].Value;

        var matchIp = Regex.Match(html, @"wlanuserip\s*[=:]\s*[""']?(\d+\.\d+\.\d+\.\d+)[""']?");
        if (matchIp.Success) session.WlanUserIp = matchIp.Groups[1].Value;

        var matchLogger = Regex.Match(html, @"loggerId\s*[=:]\s*[""']?([^""'&]+)[""']?");
        session.LoggerId = matchLogger.Success 
            ? matchLogger.Groups[1].Value 
            : $"{DateTime.Now:yyyyMMddHHmmssfff}+{username}";

        var matchSsid = Regex.Match(html, @"ssid\s*[=:]\s*[""']?([^""'&]+)[""']?");
        if (matchSsid.Success) session.Ssid = matchSsid.Groups[1].Value;

        return session;
    }
}
```

---

## 🔗 Referencias

- **Portal Nauta:** https://secure.etecsa.net:8443/
- **ETEC SA:** Empresa de Telecomunicaciones de Cuba
- **Nauta Hogar:** Servicio de internet residencial
- **Nauta Plus:** Servicio de internet móvil

---

## 📚 Recursos Adicionales

### Proyecto de Referencia
- **Repositorio:** `D:\PROYECTOS\otros NO TOCAR\nauta-auto-login\nauta-maui`
- **Stack:** .NET MAUI 8.0, C# 12
- **Archivo principal:** `Services/NautaAuthService.cs`

### Archivos Clave del Proyecto
```
nauta-maui/
├── Services/
│   ├── NautaAuthService.cs      ← Implementación completa
│   ├── INautaAuthService.cs     ← Interfaz
│   └── WindowsAuthService.cs    ← Integración con Windows
├── ViewModels/
│   └── MainViewModel.cs         ← Lógica de UI y estado
└── Views/
    └── AccessPage.xaml          ← Interfaz de usuario
```

---

**Última actualización:** 2026-03-31  
**Versión:** 1.0.0  
**Basado en:** nauta-maui project
