package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class DatabaseReadandWrite {

    val db = Firebase.firestore

    fun writeUser(user: User) {

    }

    fun readUser(): User {

        var readUser: User = User()
        return readUser
    }

    fun checkLogin(Email: String, Password: String, callback: (Boolean) -> Unit) {
        db.collection("users")
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

    fun loginUser(email: String, password: String, onUserLoaded: (User?) -> Unit){
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // Step 1: Authenticate the user using email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Step 2: After successful authentication, fetch the user data from Firestore
                    val userId = auth.currentUser?.uid
                    userId?.let {
                        db.collection("Users").document(it).get()
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
