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
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.toObject
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.CLIENT_ID
import java.io.IOException
import java.util.concurrent.TimeUnit


class StartActivity : AppCompatActivity() {

    //Google SSO Variables
    val credManager = CredentialManager.create(this)
    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private lateinit var oneTapClient: SignInClient
    private lateinit var contactDao: ContactDao
    private lateinit var messageDao: messageDao
    private var lUser: User = User()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private var mOkHttpClient = OkHttpClient.Builder().build()
    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        // Initialize DAOs
        contactDao = roomDB.getDatabase(this)!!.contactDao()!!
        messageDao = roomDB.getDatabase(this)!!.messageDao()!!

        // Clear contacts and messages on app startup
        lifecycleScope.launch(Dispatchers.IO) {
            contactDao.clearContacts()
            messageDao.clearMessages()
        }

        //Sign In Button
        var btnSignIn = findViewById<TextView>(R.id.tvSignIn)
        btnSignIn.setOnClickListener {
            val intent: Intent = Intent(
                this,
                Login_Main::class.java
            )
            startActivity(intent)
        }
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userID", null)

        if (currentUserId != null) {
            fetchUserData(currentUserId)
            Log.e(TAG, "Got Session")
            startActivity(Intent(this, ProfileUI::class.java))
            finish() // Close the login activity
        } else {
            Log.e(TAG, "No session")

            // Sign In Button
            val btnSignIn = findViewById<TextView>(R.id.tvSignIn)
            btnSignIn.setOnClickListener {
                val intent = Intent(this, Login_Main::class.java)
                startActivity(intent)
            }

            // Sign Up Button
            val btnSignUp = findViewById<Button>(R.id.btnSignUp)
            btnSignUp.setOnClickListener {
                val intent = Intent(this, Register_Permissions::class.java)
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
                            .setServerClientId(ClientID.server_client_id) // Ensure this is defined
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

    private fun fetchUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("Users")

        // Query to find the user document by userId
        usersCollection.whereEqualTo("email", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    if (userDocument != null) {
                        lUser = userDocument.toObject<User>()!! // Assuming User data class exists
                        loggedUser.user = lUser
                    } else {
                        Log.d(TAG, "No such document")
                    }
                } else {
                    Log.d(TAG, "User not found")
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




}


