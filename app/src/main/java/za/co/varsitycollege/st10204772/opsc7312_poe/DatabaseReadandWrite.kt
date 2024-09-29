package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class DatabaseReadandWrite {

    private val db = Firebase.firestore

    /**
     * Write user data to Firestore after registration.
     */
    fun registerUser(user: User, onComplete: (Boolean, String?) -> Unit) {
    }

    fun readUser(): User {

        var readUser: User = User()
        return readUser
    }

    fun checkLogin(Email: String, Password: String, callback: (Boolean) -> Unit) {
        db.collection("Users")
            .whereEqualTo("Email", Email)
            .whereEqualTo("Password", Password)// Assuming "cell" is the field in the database
            .get()
            .addOnSuccessListener { result ->
                val found = !result.isEmpty
                callback(found)  // Return `true` if details were found, otherwise `false`
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                callback(false)  // Return `false` in case of an error
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

    fun CreateUser(
        email: String,
        password: String,
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
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
    fun writeSpotifyData(data: SpotifyData) {

    }

    fun loadProfileImages(userId: String, context: Context, callback: (List<Bitmap>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val profileImages = mutableListOf<Bitmap>() // Mutable list to hold loaded Bitmaps

        // Retrieve user document from Firestore
        db.collection("Users").document(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Retrieve image URLs
                        val imageUrls = (documentSnapshot.get("ProfilePhotos") as? List<String>)
                            ?: emptyList()

                        val imageLoadCount = imageUrls.size
                        var imagesLoaded = 0

                        for (imageUrl in imageUrls) {
                            Glide.with(context)
                                .asBitmap()
                                .load(Uri.parse(imageUrl))
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        profileImages.add(resource) // Add loaded Bitmap to the list
                                        imagesLoaded++

                                        // Notify callback once all images are loaded
                                        if (imagesLoaded == imageLoadCount) {
                                            callback(profileImages) // Return the list
                                        }
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // Handle any cleanup if necessary
                                    }
                                })
                        }

                        // If there are no images, call the callback immediately
                        if (imageLoadCount == 0) {
                            callback(profileImages)
                        }
                    } else {
                        // Document does not exist
                        callback(emptyList())
                    }
                } else {
                    // Handle the error
                    callback(emptyList())
                }
            }
    }
}
