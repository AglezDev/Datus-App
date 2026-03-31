package datus.app.com.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import datus.app.com.data.db.AppDatabase
import datus.app.com.data.db.TasaHistoricaDao
import datus.app.com.data.local.DataStoreManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "datus_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideTasaHistoricaDao(appDatabase: AppDatabase): TasaHistoricaDao {
        return appDatabase.tasaHistoricaDao()
    }
    
    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }
}
