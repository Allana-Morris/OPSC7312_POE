package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DatabaseReadandWrite {

    private val db = Firebase.firestore

    /**
     * Write user data to Firestore after registration.
     */
    fun registerUser(user: User, onComplete: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        // Create a user with email and password
        auth.createUserWithEmailAndPassword(user.Email, user.Password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the user's UID and store it in Firestore
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        db.collection("users").document(it).set(user)
                            .addOnSuccessListener {
                                onComplete(true, "User registered successfully!")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error writing user to Firestore", e)
                                onComplete(false, "Failed to save user data: ${e.message}")
                            }
                    } ?: onComplete(false, "User ID not found after registration.")
                } else {
                    // Registration failed
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    onComplete(false, "Registration failed: $errorMessage")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error during registration", e)
                onComplete(false, "Registration failed: ${e.message}")
            }
    }

    /**
     * Login an existing user using FirebaseAuth.
     * Now returns `FirebaseUser` to provide user ID and authentication details.
     */
    fun loginUser(email: String, password: String, onUserLoaded: (FirebaseUser?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        // Authenticate with FirebaseAuth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Pass back the authenticated `FirebaseUser` object
                    val user = auth.currentUser
                    onUserLoaded(user)
                } else {
                    // Authentication failed
                    Log.e(TAG, "Authentication failed", task.exception)
                    onUserLoaded(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error during login", e)
                onUserLoaded(null)
            }
    }

    /**
     * Check if user profile exists and is complete.
     * This method checks if key fields such as profile image URLs and Spotify user ID are populated.
     */
    fun checkUserProfileExists(userId: String, callback: (Boolean) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Log the found document for debugging purposes
                    Log.d(TAG, "User profile found for ID: $userId")

                    // Check if key fields like profile images or Spotify ID exist
                    val profileImageUrls = document.get("profileImageUrls") as? List<String>
                    val spotifyUserId = document.getString("spotifyUserId")

                    if (!profileImageUrls.isNullOrEmpty() && !spotifyUserId.isNullOrEmpty()) {
                        Log.d(TAG, "Profile is complete for user: $userId")
                        callback(true)  // Profile is complete
                    } else {
                        Log.d(TAG, "Profile is incomplete for user: $userId")
                        callback(false)  // Profile is incomplete
                    }
                } else {
                    Log.d(TAG, "No user profile found for ID: $userId")
                    callback(false)  // Profile doesn't exist
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking user profile: ${exception.message}")
                callback(false)  // Error fetching profile, assume incomplete
            }
    }

    /**
     * Example method for reading Spotify data.
     */
    fun readSpotifyData() {
        // Add logic to read Spotify data from Firestore or any other source.
    }

    /**
     * Example method for writing Spotify data.
     */
    fun writeSpotifyData(data: SpotifyData) {
        // Add logic to write Spotify data to Firestore or any other source.
    }
}
