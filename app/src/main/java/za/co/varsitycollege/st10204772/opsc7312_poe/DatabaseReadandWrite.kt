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
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(cell) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}