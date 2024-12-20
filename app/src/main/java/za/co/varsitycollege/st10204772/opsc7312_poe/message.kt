package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "messages")
class Message {
    @PrimaryKey()
    var id: String = ""

    var fromUid: String? = null
    var toUid: String? = null
    var content: String? = null
    var timeStamp: String? = null
    var type: String? = null
    var fcmToken: String? = null
}


