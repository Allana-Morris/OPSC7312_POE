package za.co.varsitycollege.st10204772.opsc7312_poe

import android.graphics.Bitmap
import java.util.Date

class User(

    var cellnum: String,
    var name: String,
    var DOB: Date,
    var Age: Short,
    var email: String,
    var gender: String,
    var genderLabel: String,
    var pronoun: String,
    var profilePhotos: MutableList<Bitmap> = mutableListOf(),
    var spotifyUsername: String,
    var spotifyUserId: String
    ) {
     data class GoogleUser(
         var googleID: String,
         var email: String,
         var emailVerified: Boolean = false,
         var name: String,
         var pictureUrl: String,
         var locale: String,
         var familyName: String,
         var givenName: String)
     {
         fun setGoogleUser(GoogleID: String = this.googleID, Email: String = this.email, EmailVerified: Boolean = this.emailVerified, Name: String = this.name, PictureUrl: String = this.pictureUrl, Locale: String = this.locale, FamilyName: String = this.familyName, GivenName: String = this.givenName){
             this.googleID = GoogleID
             this.email = Email
             this.emailVerified = EmailVerified
             this.name= Name
             this.pictureUrl = PictureUrl
             this.locale = Locale
             this.familyName = FamilyName
             this.givenName = GivenName
         }
    }

}