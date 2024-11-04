package za.co.varsitycollege.st10204772.opsc7312_poe

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_user")
class LocalUser(
    @PrimaryKey(autoGenerate = true) var id: Int = 0, // Unique ID for Room
    var name: String = "",
    var age: Int = 0,
    var pronoun: String = "",
    var email: String = ""
)
