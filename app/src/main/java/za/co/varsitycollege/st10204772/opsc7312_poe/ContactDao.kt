package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Cont)

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Cont>

    @Query("SELECT * FROM contacts WHERE email = :email LIMIT 1")
    suspend fun getContactByEmail(email: String): Cont?

    @Query("DELETE FROM contacts")
    suspend fun clearContacts()

    // New query to get the latest message timestamp between two users
    @Query("""
        SELECT MAX(timestamp) 
        FROM messages 
        WHERE (fromUid = :fromUid AND toUid = :toUid) 
           OR (fromUid = :toUid AND toUid = :fromUid)
    """)
    suspend fun getLatestMessageTimestamp(fromUid: String, toUid: String): Long?
}