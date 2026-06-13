# Datus - Resumen de Sesiones

## Estado Actual (12 Junio 2026)

### App Android
- **Versión**: 2.2.0
- **Repo**: https://github.com/ADNova-Design/Datus-App
- **APK**: build/outputs/apk/debug/app-debug.apk

---

## Cambios Realizados

### Sesión: Modernización UI Completa + Iconos + Fixes

#### 1. NautaAuthService (sesiones anteriores)
- Servicio completo de autenticación al portal `secure.etecsa.net:8443`
- Login/logout/refresh con cookie management
- SSL bypass para certificado autofirmado

#### 2. Sistema de Tema
- **Dimens.kt**: sistema de espaciado unificado (xs/4dp a xxl/48dp)
- **Theme.kt**: soporte `dynamicColorScheme` (Android 12+) + `AmoledDarkColorScheme` (true black)
- **ThemeOption.kt**: nuevo modo `AMOLED`
- **SettingsScreen.kt**: toggle AMOLED + Dynamic Color

#### 3. Componentes UI
- **ModernIcon.kt**: icono con contenedor redondeado (default 40dp container, 22dp icon, 12dp radius)
- **DatusCard.kt**: card unificado ElevatedCard/Card
- **NavBar**: esquinas superiores planas, transiciones `AnimatedContent`
- **Surface** con bottom rounded corners sobre NavBar (efecto flotante)

#### 4. Pantalla de Consultas (QueriesScreen)
- **Fix**: PromotionsCarousel ahora condicional — solo renderiza cuando hay promos o está cargando
- **Fix**: Restaurados los 6 items originales en la grilla (Saldo Principal, Límite, Vigencia Datos, Bonos, Vigencia Voz, Vigencia SMS)
- Dashboard con 3 SummaryCards (Saldo/Datos/Bono) + grilla compacta 3-columnas
- Sin tasas de cambio

#### 5. Modernización de Iconos (NUEVO en esta sesión)
Reemplazados ~30 `Icon()` planos por `ModernIcon()` con contenedor `primaryContainer` en:

| Pantalla | Cantidad | Ubicaciones |
|---|---|---|
| **MenuScreen** | 3 | MenuCard (Settings, WhatsApp, Share) |
| **QueriesScreen** | 6 | CompactQueryCard de la grilla |
| **SettingsScreen** | 6 | Notificaciones, Tema, AMOLED, Color dinámico, Actualizaciones, WhatsApp |
| **UserSettingsScreen** | 2 | Datos para operaciones, PIN de transferencia |
| **UtilitiesScreen** | 2 | UtilityActionCard, SmallActionCard (ImageVector) |
| **TarjetaScreen** | 4 | Autenticarse, Desconectar, Saldo de Tarjeta, Recargar |
| **PlansScreen** | 4 | PlanCard (Icons.Filled.*) — Bolsa Diaria, Plan ToDus, DataUsage, MoreHoriz |
| **NautaSettingsScreen** | 5 | 3 SettingsCards + PersonOff + PersonAdd + AddCircle |
| **NautaLoginScreen** | 1 | LoggedInContent (wifi, ya existente) |
| **SharedUI** | 2 | ConfirmationDialog, SettingsDialog (ya existente) |

Tamaños usados:
- Default: 40dp container / 22dp icon
- Cards principales: 48dp container / 28dp icon
- Icono WhatsApp: `containerColor = Color(0xFF25D366).copy(alpha = 0.15f)`, `tint = Color(0xFF25D366)`

#### 6. Fixes
- `NautaLoginScreen.kt`: import `RoundedCornerShape` faltante
- `SettingsScreen.kt`: parámetro `iconTint` corregido a `tint`
- `ADB_VENDOR_KEYS`: configurado `$env:ADB_VENDOR_KEYS = "$env:USERPROFILE\.android"` para autorizar dispositivo

---

### Sesiones Anteriores (Landing Page + Tasas + Notificaciones)

1. **Notificaciones**: abren Mercado al tocarlas, default 8:00 AM
2. **Exchange Rates**: web scraping eltoque.com + fallback rates, validación VPN
3. **Mercado Screen**: datos cacheados offline
4. **Landing Page**: Cloudflare Pages, Tailwind CSS, diseño responsive
5. **Rediseño Profesional**: cards con colores por categoría, animaciones fade-in-up

---

## Tech Stack

- **Android**: Kotlin, Jetpack Compose, Hilt, ViewModel, Coil, DataStore
- **Backend**: Supabase (Postgres, Auth, Edge Functions)
- **Landing Page**: HTML, Tailwind CSS, Bootstrap Icons, Cloudflare Pages
- **Autenticación Nauta**: HTTPS propio con SSL bypass

---

## Contacto

- **WhatsApp**: +53 59072053
- **GitHub**: https://github.com/ADNova-Design/Datus-App
- **Landing**: https://datus.pages.dev
