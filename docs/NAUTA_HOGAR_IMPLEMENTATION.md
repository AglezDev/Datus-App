# Resumen: Implementación de Pantalla Nauta Hogar

## Objetivo
Implementar pantalla de login para WiFi Nauta Hogar (ETECSA) en app Android Datus

## Requisitos
- Pantalla accesible desde barra de navegación inferior
- Botón "Nauta" con icono WiFi, posición antes de "Útiles"
- Iniciar sesión con credenciales Nauta
- Mostrar datos reales de cuenta
- Opción de recordar usuario
- Botón de desconectar

---

## Implementación

### Archivos Modificados/Creados

| Archivo | Descripción |
|---------|-------------|
| `NavRoutes.kt` | Agregada ruta `NAUTA_LOGIN` |
| `MainActivity.kt` | Agregado BottomNavItem "Nauta" + navegación |
| `NautaAuthService.kt` | Servicio de auth + consulta balance real |
| `NautaLoginScreen.kt` | UI completa (login + cuenta conectada) |
| `DataStoreManager.kt` | Persistencia de credenciales |
| `NetworkModule.kt` | Configuración cookies Ktor |
| `DatabaseModule.kt` | Provide DataStoreManager |

### Endpoints Utilizados

| Operación | URL | Método |
|-----------|-----|--------|
| Login | `https://secure.etecsa.net:8443/Login` | POST |
| Balance | `https://secure.etecsa.net:8443/EtecsaQueryServlet` | POST |
| Logout | `https://secure.etecsa.net:8443/Logout` | GET |

### Parámetros para Consulta de Balance

```json
{
  "op": "getLeftTime",
  "ATTRIBUTE_UUID": "<token_sesión>",
  "CSRFHW": "<token_csrf>",
  "wlanuserip": "<ip_usuario>",
  "username": "usuario@nauta.com.cu"
}
```

### Investigación de Proyectos Existentes

- **suitetecsa-sdk-python**: SDK Python más completo
- **nautapy**: Librería Python simple
- **Nothing.Nauta**: SDK .NET

Todos usan el mismo endpoint `EtecsaQueryServlet` con `op=getLeftTime`.

---

## Estado: ✅ Completado

- Login con credenciales Nauta
- Recordar usuario (checkbox)
- Consulta de balance real desde portal ETECSA
- Mostrar: usuario, tiempo consumido, tiempo restante
- Botón "Actualizar" para refresh
- Botón "Desconectar"
