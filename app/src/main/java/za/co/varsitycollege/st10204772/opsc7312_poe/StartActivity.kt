package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth


class StartActivity : AppCompatActivity() {

    //Google SSO Variables
    val credManager = CredentialManager.create(this)
    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private lateinit var oneTapClient: SignInClient

    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

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
        var btnGoogle = findViewById<Button>(R.id.btnSignUpWithGoogle)
        btnGoogle.setOnClickListener {
            auth = Firebase.auth
            oneTapClient = Identity.getSignInClient(this)

            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(ClientID.server_client_id)
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
                    e.localizedMessage?.let { it1 -> Log.d(TAG, it1) }
                }


        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = googleCredential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        val user = auth.currentUser
                                        user?.let {
                                            User.GoogleUser().apply {
                                                name = it.displayName.toString()
                                                email = it.email.toString()
                                                pictureUrl = it.photoUrl.toString()
                                                googleID = it.uid
                                            }
                                             User().apply {
                                                Email = it.email.toString()
                                                hasGoogle = true
                                            }
                                        }
                                        val intent = Intent(this, Register_About_You::class.java)
                                        startActivity(intent)

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)

                                    }
                                }
                        }

                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }

                } catch (e: ApiException) {
                    Log.d(
                        TAG,
                        "Gad, what have you done? You're a Pink Pony girl and you dance at the club"
                    )
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d(TAG, "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            var showOneTapUI = false
                        }

                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d(TAG, "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }

                        else -> {
                            Log.d(
                                TAG, "Couldn't get credential from result." +
                                        " (${e.localizedMessage})"
                            )

                        }
                    }
                }


            }
        }
    }
}
