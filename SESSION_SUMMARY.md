# Datus - Resumen de Sesiones

## Estado Actual (30 Marzo 2026)

### App Android
- **Versión**: 2.1.5
- **Repo**: https://github.com/ADNova-Design/Datus-App
- **APK**: https://github.com/ADNova-Design/Datus-App/releases/latest/download/Datus-v2.1.5.apk

### Landing Page
- **URL**: https://datus.pages.dev
- **Deploy**: Cloudflare Pages

---

## Cambios Realizados

### Sesión: Fixes y Mejoras de la App

1. **Notificaciones**
   - Fixed: Las notificaciones ahora abren Mercado cuando se tocan
   - Set default notification a 8:00 AM habilitada

2. **Exchange Rates**
   - Replaced API con web scraping de eltoque.com
   - Agregados fallback rates: USD 515, EUR 580, MLC 400, CAD 337.65, MXN 26.74, CLA 503.55
   - Fixed: Validación para evitar mostrar 1=1 (VPN issue)
   - Fixed: Ahora detecta cualquier tipo de internet, no solo mobile data

3. **Mercado Screen**
   - Ahora muestra datos cacheados cuando está offline

4. **UI/UX**
   - Trend indicators en las cards comparando con tasas anteriores

---

### Sesión: Landing Page (v1)

1. **Creación inicial**
   - Landing page moderno con Tailwind CSS + Bootstrap Icons
   - Fuente: Outfit para títulos, Inter para texto
   - Secciones: Hero, Funciones, Tasas, Descargar, WhatsApp

2. **Problemas corregidos**
   - Icono `bi-keyboard` (no `bi-keypad`)
   - Removed GitHub link del footer
   - Fallback SVG para el logo

---

### Sesión: Mejoras de Diseño

1. **Mobile Responsiveness**
   - Enhanced CSS para móviles
   - Agregado mobile header
   - Improved rates grid

2. **Fix tasas de cambio**
   - Mejorada lógica: intenta API → scrape → fallback
   - Validación de valores (> 1)

---

### Sesión: Rediseño Profesional

1. **Diseño limpio profesional**
   - Cards con border-radius apropiado (1rem-1.5rem)
   - Colores consistentes por categoría:
     - Azul: SIM, Tasas
     - Emerald: Transferencias
     - Purple: Bonos
     - Amber: Notificaciones
   - Animaciones suaves fade-in-up

2. **Iconos corregidos**
   - `bi-keyboard` (no `bi-keypad`)

---

### Sesión: Header y Menú

1. **Header unificado**
   - Funciona en todas las pantallas
   - Fixed position con backdrop blur
   - Effecto scrolled (se oscurece al hacer scroll)

2. **Menú Hamburguesa (Mobile)**
   - Click abre slide panel desde la derecha
   - Animación X para el hamburger
   - Overlay oscuro
   - Links: Funciones, Tasas, Descargar, WhatsApp

---

### Sesión: Deploy Cloudflare

1. **Creado proyecto**: `datus` en Cloudflare Pages
2. **Desplegado a**: https://datus.pages.dev
3. **Wrangler configurado** con OAuth

---

## Pendientes / Mejoras Futuras

- [ ] Testing en dispositivo real desde Cuba (VPN-less)
- [ ] Agregar más currencies a las tasas
- [ ] Widget de tasas para Android
- [ ] Notificaciones push
- [ ] Analytics en el landing page

---

## Tech Stack

- **Android**: Kotlin, Jetpack Compose, ViewModel, Web Scraping
- **Landing Page**: HTML, Tailwind CSS, Bootstrap Icons
- **Hosting**: Cloudflare Pages
- **APK Hosting**: GitHub Releases

---

## Contacto

- **WhatsApp**: +53 59072053
- **GitHub**: https://github.com/ADNova-Design/Datus-App
- **Landing**: https://datus.pages.dev
