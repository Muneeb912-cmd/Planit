package com.example.eventmanagement.di

import android.content.Context
import androidx.room.Room
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.LocalDB
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.repository.roomDb.attendee_dao.AttendeeDao
import com.example.eventmanagement.repository.roomDb.events_dao.EventDao
import com.example.eventmanagement.repository.roomDb.fav_events_dao.FavEventsDao
import com.example.eventmanagement.repository.roomDb.invites_dao.InvitesDao
import com.example.eventmanagement.repository.roomDb.user_dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): LocalDB {
        return Room.databaseBuilder(
            appContext,
            LocalDB::class.java,
            "LocalDB"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: LocalDB): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideAttendeeDao(database: LocalDB): AttendeeDao {
        return database.attendeeDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: LocalDB): EventDao {
        return database.eventsDao()
    }

    @Provides
    @Singleton
    fun provideFavEventsDao(database: LocalDB): FavEventsDao {
        return database.favEventsDao()
    }

    @Provides
    @Singleton
    fun provideInvitesDao(database: LocalDB): InvitesDao {
        return database.invitesDao()
    }

    @Provides
    @Singleton
    fun providePendingOperationsDao(database: LocalDB): PendingOperationDao {
        return database.pendingOperationsDao()
    }

    @Provides
    @Singleton
    fun provideConverters(): Converters {
        return Converters()
    }
}
