package com.example.eventmanagement.di

import com.example.eventmanagement.utils.Validators
import com.example.eventmanagement.utils.implementations.ValidatorsImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideValidators(): Validators = ValidatorsImpl()
}