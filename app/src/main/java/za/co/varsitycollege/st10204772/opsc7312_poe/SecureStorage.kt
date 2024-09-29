package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveID(secret: String, secrettitle: String) {
        sharedPreferences.edit().putString(secrettitle, secret).apply()
    }

    fun getID( secrettitle: String): String? {
        return sharedPreferences.getString( secrettitle, "")
    }
}
