# ============================================
# DATUS APP - ProGuard Rules
# ============================================

# Keep source file names for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# HILT / DAGGER
# ============================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keepnames class * { @dagger.hilt.android.lifecycle.HiltViewModel <init>(...); }
-keep class * extends dagger.hilt.android.internal.managers.HiltWrapper_HiltModulesKeyModule { *; }
-keep class dagger.hilt.android.internal.managers.HiltWrapper_HiltModulesKeyModule
-keep class * extends dagger.hilt.internal.GeneratedInjectorBase { *; }
-keep class * implements dagger.hilt.internal.GeneratedInjector { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * implements dagger.hilt.internal.EntryPoint { *; }
-keep class * implements dagger.hilt.InstallIn { *; }
-keep class * extends dagger.hilt.android.internal.modules.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint { *; }
-dontwarn dagger.internal.codegen.**
-dontwarn dagger.hilt.android.internal.**

# ============================================
# ROOM
# ============================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Database class *
-dontwarn androidx.room.paging.**

# ============================================
# SUPABASE
# ============================================
-keep class io.github.jan_tennert.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan_tennert.supabase.**

# ============================================
# KTOR
# ============================================
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations
-keepattributes EnclosingMethod

# ============================================
# KOTLINX SERIALIZATION
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.yourcompany.yourpackage.**$$serializer { *; }
-keepclassmembers class com.yourcompany.yourpackage.** {
    *** Companion;
}
-keepclasseswithmembers class com.yourcompany.yourpackage.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.json.** { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }
-dontwarn kotlinx.serialization.**

# ============================================
# KOTLIN REFLECTION
# ============================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.coroutines.** { *; }

# ============================================
# COMPOSE
# ============================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================
# COIL
# ============================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================
# DATASTORE
# ============================================
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ============================================
# NAVIGATION COMPOSE
# ============================================
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ============================================
# LIFECYCLE
# ============================================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.lifecycle.ViewModel
-dontwarn androidx.lifecycle.**

# ============================================
# ZXING
# ============================================
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }
-dontwarn com.google.zxing.**

# ============================================
# QR GENERATOR
# ============================================
-keep class io.github.alexzhirkevich.** { *; }
-dontwarn io.github.alexzhirkevich.**

# ============================================
# JSOUP
# ============================================
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# ============================================
# GENERAL
# ============================================
-keep class datus.app.com.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @dagger.Module <methods>;
}
-keepclassmembers class * {
    @dagger.Provides <methods>;
}
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
}
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}