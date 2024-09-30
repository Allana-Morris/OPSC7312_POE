package za.co.varsitycollege.st10204772.opsc7312_poe

import android.graphics.Bitmap
import android.media.session.MediaSession
import java.util.Date

class User() {
    var Name: String = ""
    var DOB: Date = Date()
    var Age: Int = 0
    var Email: String = ""
    var Password: String = ""
    var Gender: String = ""
    var GenderLabel: String? = ""
    var Pronoun: String = ""
    var ProfilePhotos: MutableList<Bitmap> = mutableListOf()
    var SpotifyUsername: String = ""
    var SpotifyUserId: String = ""
    var hasGoogle: Boolean = false

    data class GoogleUser(
        var googleID: String = "",
        var email: String = "",
        var name: String = "",
        var pictureUrl: String = "",

    ) {

    }

}