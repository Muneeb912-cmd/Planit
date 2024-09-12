package com.example.eventmanagement.di

import android.content.Context
import androidx.work.WorkManager
import com.example.eventmanagement.repository.worker.SyncRepository
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Validators
import com.example.eventmanagement.utils.implementations.PreferencesImpl
import com.example.eventmanagement.utils.implementations.ValidatorsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideValidators(): Validators = ValidatorsImpl()
    @Provides
    @Singleton
    fun providePreferencesUtil(@ApplicationContext context: Context): PreferencesUtil {
        return PreferencesImpl(context)
    }
    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserver(context)
    }


    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(workManager: WorkManager): SyncRepository {
        return SyncRepository(workManager)
    }

}