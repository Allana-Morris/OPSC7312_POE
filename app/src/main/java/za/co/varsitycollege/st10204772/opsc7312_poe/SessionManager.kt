package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Session keys
    private val KEY_IS_LOGGED_IN = "isLoggedIn"

    // Save user session
    fun createLoginSession(userId: String, spotifyId: String?) {
        with(sharedPreferences.edit()) {
            putString("${userId}_spotifyId", spotifyId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSpotifyIdByUserId(userId: String): String? {
        return sharedPreferences.getString("${userId}_spotifyId", null)
    }

    // Logout user
    fun logoutUser() {
        editor.clear()
        editor.apply()
    }
}