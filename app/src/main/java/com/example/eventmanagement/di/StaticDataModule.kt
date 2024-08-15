package com.example.eventmanagement.di

import androidx.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StaticDataModule {

    @Provides
    @Categories
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
    @Provides
    @CitiesCountries
    @Singleton
    fun provideCitiesCountries(): ArrayList<String> {
        return arrayListOf(
            "Lahore, PK",
            "Karachi, PK",
            "Islamabad, PK",
            "Multan, PK",
            "Peshawar, PK",
        )
    }
}