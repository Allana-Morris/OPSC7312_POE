package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface userDao
{
    @Insert
    fun insert(user: User?)

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUser(email: String?): User?
}