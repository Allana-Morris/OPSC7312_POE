package za.co.varsitycollege.st10204772.opsc7312_poe

import android.net.Uri

class SpotifyData() {
    var email: String = "" //Foreign Key
    var spotifyId: String = "" //Primary Key
    var displayName: String? = ""
    var profpicurl: Uri = Uri.parse("")
    var apihref: Uri = Uri.parse("")
    var artistID: MutableList<String> = mutableListOf()
    var artistName: MutableList<String> = mutableListOf()
    var songName: MutableList<String> = mutableListOf()
    var albumName: MutableList<String> = mutableListOf()
    var songartistName: MutableList<String> = mutableListOf()
    var albumpicUrl: MutableList<Uri> = mutableListOf()
    var genre: MutableList<String> = mutableListOf()

}