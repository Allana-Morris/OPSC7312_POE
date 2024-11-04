package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(localUser: LocalUser)

    @Query("SELECT * FROM local_user WHERE email = :email") // Query by email
    suspend fun getUserByEmail(email: String): LocalUser?

    @Query("SELECT * FROM local_user WHERE id = :userId")
    suspend fun getUserById(userId: Int): LocalUser?

    @Query("SELECT * FROM local_user")
    suspend fun getAllUsers(): List<LocalUser>

    @Query("DELETE FROM local_user")
    suspend fun deleteAllUsers()
}
