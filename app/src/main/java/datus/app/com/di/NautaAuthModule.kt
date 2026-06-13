package datus.app.com.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import datus.app.com.services.NautaAuthService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NautaAuthModule {

    @Provides
    @Singleton
    fun provideNautaAuthService(): NautaAuthService {
        return NautaAuthService()
    }
}
