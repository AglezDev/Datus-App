package datus.app.com.di

import android.content.Context
import android.net.ConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import datus.app.com.data.local.NotificationReadStatusManager
import datus.app.com.data.remote.NotificationService
import datus.app.com.repository.NotificationRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationReadStatusManager(@ApplicationContext context: Context): NotificationReadStatusManager {
        return NotificationReadStatusManager(context)
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    @Singleton
    fun provideNotificationService(postgrest: Postgrest): NotificationService {
        return NotificationService(postgrest)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationService: NotificationService,
        notificationReadStatusManager: NotificationReadStatusManager,
        @ApplicationContext context: Context
    ): NotificationRepository {
        return NotificationRepository(notificationService, notificationReadStatusManager, context)
    }
}