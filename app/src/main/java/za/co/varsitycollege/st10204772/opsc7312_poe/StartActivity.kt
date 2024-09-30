@file:Suppress("DEPRECATION")

package za.co.varsitycollege.st10204772.opsc7312_poe

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
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class StartActivity : AppCompatActivity() {

    //Google SSO Variables
    val credManager = CredentialManager.create(this)
    //  private lateinit var auth: FirebaseAuth
    //  private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    //   private lateinit var oneTapClient: SignInClient

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        FirebaseApp.initializeApp(this)

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ClientID.server_client_id)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        var btnGoogle = findViewById<Button>(R.id.btnSignUpWithGoogle)
        btnGoogle.setOnClickListener {
            googleSignin()
        }
    }

    private fun googleSignin() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
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
