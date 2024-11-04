@file:Suppress("DEPRECATION")

package za.co.varsitycollege.st10204772.opsc7312_poe

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.util.concurrent.TimeUnit


class StartActivity : AppCompatActivity() {

    //Google SSO Variables
    val credManager = CredentialManager.create(this)
    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private lateinit var oneTapClient: SignInClient
    private var lUser: User = User()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userID", null)

        if (currentUserId != null) {
            fetchUserData(currentUserId)
            startActivity(Intent(this, ProfileUI::class.java))
            finish() // Close the login activity
        } else {
            //Sign In Button
            var btnSignIn = findViewById<TextView>(R.id.tvSignIn)
            btnSignIn.setOnClickListener {
                val intent: Intent = Intent(
                    this,
                    Login_Main::class.java
                )
                startActivity(intent)
            }

            //Sign Up Button
            var btnSignUp = findViewById<Button>(R.id.btnSignUp)
            btnSignUp.setOnClickListener {
                val intent: Intent = Intent(
                    this,
                    Register_Permissions::class.java
                )
                startActivity(intent)
            }

            // Google SSO
            val btnGoogle = findViewById<Button>(R.id.btnSignUpWithGoogle)
            btnGoogle.setOnClickListener {
                auth = Firebase.auth
                oneTapClient = Identity.getSignInClient(this)

                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(ClientID.server_client_id) // Make sure this is defined properly
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    ).build()

                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(this) { result ->
                        try {
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, REQ_ONE_TAP,
                                null, 0, 0, 0
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(this) { e ->
                        Log.d(TAG, e.localizedMessage ?: "Sign-in failed")
                    }
            }
        }
    }

    private fun fetchUserData(email: String) {
        db.collection("users") // Adjust the collection name based on your Firestore structure
            .document(email) // Use the email as the document ID
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    lUser = document.toObject<User>()!! // Assuming you have a User data class
                    loggedUser.user = lUser
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Get failed with ", exception)
            }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                        val idToken = googleCredential.googleIdToken
                        if (idToken != null) {
                            // Authenticate with Firebase
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, retrieve user information
                                        val user = auth.currentUser
                                        user?.let {
                                            val userEmail = user.email ?: "No Email"
                                            val userId = user.uid
                                            lUser.Email = userEmail
                                            loggedUser.user = lUser

                                            Toast.makeText(
                                                this,
                                                "Google Email: ${loggedUser.user?.Email.toString()}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            // Write user information to Firestore
                                            val db = FirebaseFirestore.getInstance()
                                            val usersCollection = db.collection("Users")

                                            // Check if the user document exists
                                            usersCollection.whereEqualTo("email", userEmail).get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    if (querySnapshot.isEmpty) {
                                                        // Create a new user document if it doesn't exist
                                                        val newUser = hashMapOf(
                                                            "email" to userEmail,
                                                            "hasGoogle" to true
                                                        )
                                                        usersCollection.document(userId)
                                                            .set(newUser)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    this,
                                                                    "User created in db",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                startActivity(
                                                                    Intent(
                                                                        this,
                                                                        Register_About_You::class.java
                                                                    )
                                                                )
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(
                                                                    this,
                                                                    "Error adding user: ${e.message}",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                    } else {
                                                        Log.d(TAG, "User already exists.")
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        this,
                                                        "Error querying user: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                    } else {
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                    }
                                }
                        } else {
                            Log.d(TAG, "No ID token received.")
                        }
                    } catch (e: ApiException) {
                        Log.d(TAG, "Error retrieving Google credentials: ${e.localizedMessage}")
                        when (e.statusCode) {
                            CommonStatusCodes.CANCELED -> Log.d(TAG, "One-tap dialog was closed.")
                            CommonStatusCodes.NETWORK_ERROR -> Log.d(
                                TAG,
                                "One-tap encountered a network error."
                            )

                            else -> Log.d(
                                TAG,
                                "Couldn't get credential from result. (${e.localizedMessage})"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            if (account != null) {
                // Capture user information
                User().Email = account.email.toString()
                User().hasGoogle = true

                // Write user information to Firestore
                val db = FirebaseFirestore.getInstance()
                val userId = account.id ?: ""

                val usersCollection = db.collection("Users")

                // Query to find the user document where email matches
                usersCollection.whereEqualTo("email", account.email).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            val userDocument = querySnapshot.documents[0]

                            // Update the document with the top songs
                            userDocument.reference.update("email", account.email)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Email added to db",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error adding Google Email: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            userDocument.reference.update("hasGoogle", true)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "User has Google",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            // Update your UI to show the user is signed out
                        }
                    }

            }
        }

    }
}


