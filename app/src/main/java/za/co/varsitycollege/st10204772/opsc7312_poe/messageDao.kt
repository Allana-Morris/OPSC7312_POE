package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface messageDao {
    @Insert
    fun insert(message: Message)

    // Keep two parameters for flexibility
    @Query("SELECT * FROM messages WHERE fromUid = :fromUid OR toUid = :toUid ORDER BY timestamp")
    fun getMessages(fromUid: String, toUid: String): List<Message?>

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE id = :messageId)")
    suspend fun checkMessageIdExists(messageId: String): Boolean
}
