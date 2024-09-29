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
    fun loginUser(email: String, password: String, onUserLoaded: (User?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        // Step 1: Authenticate the user using email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Step 2: After successful authentication, fetch the user data from Firestore
                    val userId = auth.currentUser
                    userId?.let {
                        db.collection("Users").document(it.uid).get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    // Step 3: Map the Firestore document into the User class
                                    val user = documentSnapshot.toObject(User::class.java)
                                    onUserLoaded(user)
                                } else {
                                    onUserLoaded(null) // No user data found
                                }
                            }
                            .addOnFailureListener { e ->
                                e.printStackTrace()
                                onUserLoaded(null)
                            }
                    }
                } else {
                    // Authentication failed
                    onUserLoaded(null)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onUserLoaded(null)
            }
    }

    fun CreateUser(email: String, password: String, user: User, onComplete: (Boolean, String?) -> Unit){
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Step 1: Create a new user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Step 2: After successful registration, get the user's UID
                    val userId = auth.currentUser?.uid
                    user.Email = email // Set the email in the user object

                    // Step 3: Save the user data to Firestore
                    userId?.let {
                        db.collection("Users").document(it).set(user)
                            .addOnSuccessListener {
                                onComplete(true, "User registered successfully!")
                            }
                            .addOnFailureListener { e ->
                                e.printStackTrace()
                                onComplete(false, "Failed to save user data: ${e.message}")
                            }
                    }
                } else {
                    // Registration failed
                    onComplete(false, "Registration failed: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(false, "Registration failed: ${e.message}")
            }
    }



    }
