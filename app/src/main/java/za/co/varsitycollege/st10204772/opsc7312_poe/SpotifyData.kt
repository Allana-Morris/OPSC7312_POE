package za.co.varsitycollege.st10204772.opsc7312_poe

import android.net.Uri

class SpotifyData(
    var email: String, //Foreign Key
    var spotifyId: String, //Primary Key
    var displayName: String?,
    var profpicurl: Uri,
    var profpicheight: Short,
    var profpicwidth: Short,
    var apihref: Uri
    ) {
    data class Artist(
        var artistID: MutableList<String>,
       var genres: MutableList<String>,
        var artistName: MutableList<String>
        ){

    }
    data class Song(
        var songID: MutableList<String>,
        var songName: MutableList<String>,
        var albumName: MutableList<String>,
        var artistID: MutableList<String>,
        var albumpicUrl: MutableList<Uri>,
        var albumpicHeight: MutableList<Short>,
        var albumpicWidth: MutableList<Short>
    )
}