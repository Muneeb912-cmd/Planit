package com.example.eventmanagement.repository.roomDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.FavEvents
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.User
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.roomDb.attendee_dao.AttendeeDao
import com.example.eventmanagement.repository.roomDb.events_dao.EventDao
import com.example.eventmanagement.repository.roomDb.fav_events_dao.FavEventsDao
import com.example.eventmanagement.repository.roomDb.invites_dao.InvitesDao
import com.example.eventmanagement.repository.roomDb.user_dao.UserDao

@Database(
    entities = [
        User.UserData::class,
        EventData::class,
        Invites::class,
        Attendees::class,
        FavEvents::class,
        PendingOperations::class
    ],
    version = 9,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class LocalDB : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun attendeeDao(): AttendeeDao
    abstract fun eventsDao(): EventDao
    abstract fun favEventsDao(): FavEventsDao
    abstract fun invitesDao(): InvitesDao
    abstract fun pendingOperationsDao(): PendingOperationDao

    companion object {
        @Volatile private var INSTANCE: LocalDB? = null

        fun getInstance(context: Context): LocalDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDB::class.java,
                    "LocalDB"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}