package datus.app.com.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import datus.app.com.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL ?: "",
            supabaseKey = BuildConfig.SUPABASE_KEY ?: ""
        ) {
            install(Auth)
            install(Postgrest)
            defaultSerializer = io.github.jan.supabase.serializer.KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
                serializersModule = kotlinx.serialization.modules.SerializersModule {
                    contextual(java.util.UUID::class, datus.app.com.data.UUIDSerializer)
                }
            })
        }
    }

    @Provides
    @Singleton
    fun provideAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }
}
