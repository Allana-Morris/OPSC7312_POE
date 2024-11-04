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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.CLIENT_ID

class StartActivity : AppCompatActivity() {

    // Google SSO Variables
    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2 // Can be any integer unique to the Activity
    private lateinit var oneTapClient: SignInClient
    private lateinit var contactDao: ContactDao
    private lateinit var messageDao: messageDao
    private lateinit var localUserDao: LocalUserDao

    private var lUser: User = User()
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var mOkHttpClient = OkHttpClient.Builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        // Initialize DAOs
        contactDao = roomDB.getDatabase(this)!!.contactDao()!!
        messageDao = roomDB.getDatabase(this)!!.messageDao()!!
        localUserDao = roomDB.getDatabase(this)!!.localUserDao()!!

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {

            contactDao.clearContacts()

        }
    }



        // Sign In Button
        findViewById<TextView>(R.id.tvSignIn).setOnClickListener {
            startActivity(Intent(this, Login_Main::class.java))
        }

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userID", null)

        if (currentUserId != null) {
            fetchUserData(currentUserId)
            Log.e(TAG, "Got Session")
            showBiometricPrompt(currentUserId) // Show biometric prompt
        } else {
            Log.e(TAG, "No session")

            // Sign Up Button
            findViewById<Button>(R.id.btnSignUp).setOnClickListener {
                startActivity(Intent(this, Register_Permissions::class.java))
            }

            // Google SSO
            findViewById<Button>(R.id.btnSignUpWithGoogle).setOnClickListener {
                auth = FirebaseAuth.getInstance()
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

    private fun showBiometricPrompt(userId: String) {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric is available and set up, so show the biometric prompt
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // User authenticated successfully, load the ProfileUI
                        navigateToProfile()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Login")
                    .setSubtitle("Authenticate using your fingerprint")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Biometric hardware not available, or no fingerprint enrolled. Directly navigate to ProfileUI.
                navigateToProfile()
            }
        }
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileUI::class.java))
        finish()
    }

    private fun fetchUserData(userId: String) {
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
                                "Unable to resolve sign-in status: ${e.statusCode}"
                            )
                        }
                    }
                }
            }
        }
    }
}
