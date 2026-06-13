package datus.app.com.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta proporcionada por el usuario
val AzulMuyOscuro = Color(0xFF0B1F3B)
val AzulProfundo = Color(0xFF123A63)
val AzulMedio = Color(0xFF2F5D8C)
val AzulGrisaceoClaro = Color(0xFFC9D6E5)
val BlancoAzuladoMuySuave = Color(0xFFF2F5F8)

// Mapeo a tokens de Material Design 3 (Tema Claro)
val md_theme_light_primary = AzulProfundo 
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = AzulGrisaceoClaro.copy(alpha = 0.4f)
val md_theme_light_onPrimaryContainer = AzulMuyOscuro
val md_theme_light_secondary = AzulMedio
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = BlancoAzuladoMuySuave
val md_theme_light_onSecondaryContainer = AzulProfundo
val md_theme_light_tertiary = AzulMuyOscuro
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = AzulGrisaceoClaro
val md_theme_light_onTertiaryContainer = AzulMuyOscuro
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)

// Manteniendo blanco puro para fondo por peticion del usuario, pero usando la paleta para variaciones
val md_theme_light_background = Color(0xFFFFFFFF) 
val md_theme_light_onBackground = AzulMuyOscuro
val md_theme_light_surface = Color(0xFFFFFFFF) 
val md_theme_light_onSurface = AzulMuyOscuro
val md_theme_light_surfaceVariant = BlancoAzuladoMuySuave // Usamos el tono mas claro de la paleta
val md_theme_light_onSurfaceVariant = AzulMedio
val md_theme_light_outline = AzulGrisaceoClaro
val md_theme_light_inverseOnSurface = BlancoAzuladoMuySuave
val md_theme_light_inverseSurface = AzulMuyOscuro
val md_theme_light_inversePrimary = AzulGrisaceoClaro
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color.Transparent // Forzado transparente para evitar tintes
val md_theme_light_outlineVariant = BlancoAzuladoMuySuave
val md_theme_light_scrim = Color(0xFF000000)

// Mapeo a tokens de Material Design 3 (Tema Oscuro)
val md_theme_dark_primary = AzulGrisaceoClaro
val md_theme_dark_onPrimary = AzulMuyOscuro
val md_theme_dark_primaryContainer = AzulProfundo
val md_theme_dark_onPrimaryContainer = AzulGrisaceoClaro
val md_theme_dark_secondary = AzulMedio
val md_theme_dark_onSecondary = AzulMuyOscuro
val md_theme_dark_secondaryContainer = AzulProfundo
val md_theme_dark_onSecondaryContainer = BlancoAzuladoMuySuave
val md_theme_dark_tertiary = AzulGrisaceoClaro
val md_theme_dark_onTertiary = AzulMuyOscuro
val md_theme_dark_tertiaryContainer = AzulMedio
val md_theme_dark_onTertiaryContainer = BlancoAzuladoMuySuave
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF000000) // AMOLED
val md_theme_dark_onBackground = BlancoAzuladoMuySuave
val md_theme_dark_surface = Color(0xFF101418)
val md_theme_dark_onSurface = BlancoAzuladoMuySuave
val md_theme_dark_surfaceVariant = AzulMuyOscuro
val md_theme_dark_onSurfaceVariant = AzulGrisaceoClaro
val md_theme_dark_outline = AzulMedio
val md_theme_dark_inverseOnSurface = AzulMuyOscuro
val md_theme_dark_inverseSurface = BlancoAzuladoMuySuave
val md_theme_dark_inversePrimary = AzulProfundo
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color.Transparent
val md_theme_dark_outlineVariant = AzulProfundo
val md_theme_dark_scrim = Color(0xFF000000)

val seed = AzulProfundo
val GrayLight = Color(0xFFE0E0E0)
