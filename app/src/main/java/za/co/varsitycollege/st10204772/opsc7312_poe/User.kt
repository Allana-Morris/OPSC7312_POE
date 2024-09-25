package za.co.varsitycollege.st10204772.opsc7312_poe

import android.graphics.Bitmap
import java.util.Date

class User() {
    var Cellnum: String = ""
    var Name: String = ""
    var DOB: Date = Date()
    var Age: Int = 0
    var uEmail: String = ""
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
        var emailVerified: Boolean = false,
        var name: String = "",
        var pictureUrl: String = "",
        var locale: String = "",
        var familyName: String = "",
        var givenName: String = "",
    ) {
       fun connectUser(user: User)
       {
           user.uEmail = email
           user.Name = givenName
           user.hasGoogle = true
       }
    }



    fun setUsercell(cellNum: String) {
        Cellnum = cellNum
    }

    fun setUserDetails(name: String, dob: Date, age: Int, gender: String, subgender: String?, pronouns: String){
        Name = name
        DOB = dob
        Age = age
        Gender = gender
        GenderLabel = subgender
        Pronoun = pronouns
    }

    fun getUsercell(): String{
        return Cellnum
    }
}