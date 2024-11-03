package za.co.varsitycollege.st10204772.opsc7312_poe

import com.google.firebase.firestore.FirebaseFirestore

object UserStatusManager {

    fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.update("isOnline", isOnline).addOnFailureListener {
            // Handle error
        }
    }

    fun isUserOnline(userId: String, callback: (Boolean) -> Unit) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val isOnline = document.getBoolean("isOnline") ?: false
                callback(isOnline)
            } else {
                callback(false) // User document doesn't exist
            }
        }.addOnFailureListener {
            callback(false) // Handle error
        }
    }
}