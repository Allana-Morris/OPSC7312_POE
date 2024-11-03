package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
    class Cont {
    @PrimaryKey
    var email: String =""
    var name: String? = null
    var lastMessageId: String?   = null     // ID of the last message document, if needed
}
