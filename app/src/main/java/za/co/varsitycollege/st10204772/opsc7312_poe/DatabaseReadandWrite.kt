package za.co.varsitycollege.st10204772.opsc7312_poe

import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class DatabaseReadandWrite {

    fun writeUser(user: User){

    }

    fun readUser(): User {

        var readUser: User = User()
        return readUser
    }

    fun readSpotifyData(){

    }

    fun writeSpotifyData(data: SpotifyData ){

    }

    fun checkUser(cell: String): Boolean{

        return true
    }

    fun sendOTP(cell: String){

    }
}