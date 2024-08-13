package com.example.eventmanagement.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StaticDataModule {

    @Provides
    @Singleton
    fun provideCategories(): ArrayList<String> {
        return arrayListOf(
            "Concerts",
            "Sports",
            "Conferences",
            "Theater",
            "Festivals",
            "Workshops",
            "Networking Events",
            "Family Events",
            "Charity Events",
            "Exhibitions"
        )
    }

}