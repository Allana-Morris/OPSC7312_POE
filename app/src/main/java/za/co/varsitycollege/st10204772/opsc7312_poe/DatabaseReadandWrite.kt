package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
     * Check if a user exists based on email and password.
     */
    fun checkLogin(email: String, callback: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        // Attempt to sign in with FirebaseAuth
        auth.signInWithEmailAndPassword(email, "dummy_password")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If authentication is successful, user exists
                    callback(true)
                } else {
                    // User not found or authentication failed
                    callback(false)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error checking login", e)
                callback(false) // Return false in case of failure
            }
    }


    /**
     * Login an existing user using FirebaseAuth.
     */
    fun loginUser(email: String, password: String, onUserLoaded: (User?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        // Authenticate with FirebaseAuth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If login is successful, fetch the user's profile from Firestore
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        db.collection("users").document(it).get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val user = documentSnapshot.toObject(User::class.java)
                                    onUserLoaded(user) // User data found
                                } else {
                                    onUserLoaded(null) // No user data found in Firestore
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error fetching user data", e)
                                onUserLoaded(null)
                            }
                    } ?: onUserLoaded(null) // No UID found
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
