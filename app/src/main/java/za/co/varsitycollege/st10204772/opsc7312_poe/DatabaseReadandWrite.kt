package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class DatabaseReadandWrite {

    val db = Firebase.firestore

    fun writeUser(user: User) {

    }

    fun readUser(): User {

        var readUser: User = User()
        return readUser
    }

    suspend fun checkLogin(Email: String, Password: String): Boolean {
        return try {
            val result = db.collection("Users")
                .whereEqualTo("Email", Email)
                .whereEqualTo("Password", Password)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            Log.w(TAG, "Error getting documents.", e)
            false
        }
    }




fun readSpotifyData() {

}

fun writeSpotifyData(data: SpotifyData) {

}

fun checkUser(cell: String): Boolean {

    return true
}

fun sendOTP(cell: String) {

}
}