package za.co.varsitycollege.st10204772.opsc7312_poe

import android.net.Uri
import androidx.core.net.toUri

class SpotifyData(){
var email: String = "" //Foreign Key
var spotifyId: String = "" //Primary Key
var displayName: String? = ""
var profpicurl: Uri = "".toUri()
var apihref: Uri = "".toUri()

    data class Artists(
        var artistID: MutableList<String>,
       var genres: MutableList<String>,
        var artistName: MutableList<String>
        ){

    }
    data class Songs(
        var songID: MutableList<String>,
        var songName: MutableList<String>,
        var albumName: MutableList<String>,
        var artistID: MutableList<String>,
        var albumpicUrl: MutableList<Uri>,
    )
}