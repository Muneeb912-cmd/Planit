package com.example.eventmanagement.repository.roomDb.user_dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventmanagement.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUserData(): List<User.UserData>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getCurrentUser(userId: String): User.UserData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserData(userData: User.UserData)

    @Query(
        """
    UPDATE users 
    SET 
        userName = :userName,
        userPhone = :userPhone, 
        userDob = :userDob, 
        userImg = :userImg 
    WHERE userId = :userId
    """
    )
    suspend fun updateUserProfile(
        userId: String,
        userName: String,
        userPhone: String,
        userDob: String,
        userImg: String
    ): Int

    @Query("UPDATE users SET userLocation = :newLocation WHERE userId = :userId")
    suspend fun updateUserLocation(userId: String, newLocation: String): Int

    @Query("UPDATE users SET notificationsAllowed = :newSettings WHERE userId = :userId")
    suspend fun updateUserNotificationSettings(userId: String, newSettings: Boolean): Int

    @Query("UPDATE users SET profilePrivate = :newSettings WHERE userId = :userId")
    suspend fun updateUserProfileStatus(userId: String, newSettings: Boolean): Int

    @Query("UPDATE users SET userBanned = :banStatus WHERE userId = :userId")
    suspend fun updateUserBanStatus(userId: String, banStatus: Boolean): Int

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun observeCurrentUser(userId: String): Flow<User.UserData?>

    @Query("SELECT * FROM users")
    fun observeUsers(): Flow<List<User.UserData>?>

    @Query("DELETE FROM users WHERE userId= :userId")
    suspend fun deleteUser(userId: String)
}