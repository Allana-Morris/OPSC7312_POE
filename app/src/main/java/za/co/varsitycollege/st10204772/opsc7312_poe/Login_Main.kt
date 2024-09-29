package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spotify.sdk.android.auth.AccountsQueryParameters.REDIRECT_URI
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.CLIENT_ID
import java.io.IOException

class Login_Main : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private val AUTHORIZATION_CODE = 1337
    private var mOkHttpClient = OkHttpClient.Builder().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var btnLogin = findViewById<Button>(R.id.btnContinueLogin)
        var userEmail = findViewById<EditText>(R.id.etxtEmailLogin)
        var userPassword = findViewById<EditText>(R.id.etxtPassword)
        var signup = findViewById<TextView>(R.id.txtSignUpRedirectLogin)
        var uEmail = userEmail.text
        var uPass = userPassword.text
        var inpval = InputValidation()

        signup.setOnClickListener {
            var intent = Intent(this, Register_Permissions::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            if ((inpval.isStringInput(uEmail.toString())) && (inpval.isStringInput(uPass.toString()))) {
                var email = uEmail.toString()
                var password = uPass.toString()

                if ((inpval.isEmail(email)) && (inpval.isPassword(password))) {
                    DatabaseReadandWrite().checkLogin(email, password) { isFound ->

                        if (isFound) {
                            DatabaseReadandWrite().loginUser(email, password) { user ->
                                if (user != null) {
                                    authenticateWithSpotify()
                                    var intent = Intent(this, ProfileUI::class.java)
                                    startActivity(intent)
                                } else {
                                    Log.e(TAG, "Failed to load user")
                                }
                            }
                        } else {
                            Log.e(TAG, "User Not Found or Failed to load user")
                            Toast.makeText(this, "User Not Found", Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (!inpval.isEmail(email)) {
                    Log.e(TAG, "Invalid Email")
                    Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_LONG).show()
                } else if (!inpval.isPassword(password)) {
                    Log.e(TAG, "Invalid Password")
                    Toast.makeText(this, "Invalid Password Format", Toast.LENGTH_LONG).show()
                } else {
                    Log.e(TAG, "Error (wtf bro)")
                    Toast.makeText(this, "Input is unable to be Validated", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Log.e(TAG, "Invalid Input")
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun authenticateWithSpotify() {
        val redirectUri = "YOUR_REDIRECT_URI"
         val SCOPES = "user-read-private user-read-email user-top-read" // Add scopes as needed

        val authUrl = "https://accounts.spotify.com/authorize" +
                "?client_id=$CLIENT_ID" +
                "&response_type=$AUTHORIZATION_CODE" +
                "&redirect_uri=$REDIRECT_URI" +
                "&scope=$SCOPES"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }


     override fun onNewIntent(intent: Intent) {
         if (intent != null) {
             super.onNewIntent(intent)
         }
        val uri = intent?.data
        uri?.getQueryParameter("code")?.let { authorizationCode ->
            exchangeAuthorizationCodeForToken(authorizationCode)
        }
    }

    private fun exchangeAuthorizationCodeForToken(authorizationCode: String) {
        val tokenRequest = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(
                RequestBody.create(
                    "application/x-www-form-urlencoded".toMediaTypeOrNull(),
                    "grant_type=authorization_code&code=$authorizationCode&redirect_uri=$REDIRECT_URI&client_id=$CLIENT_ID&client_secret=YOUR_CLIENT_SECRET"
                )
            )
            .build()

        mOkHttpClient.newCall(tokenRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val accessToken = jsonObject.getString("access_token")
                    val refreshToken = jsonObject.getString("refresh_token")

                    // Store tokens securely
                    saveTokens(accessToken, refreshToken)
                }
            }
        })
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        // Implement secure storage for tokens
        val storage = SecureStorage(this)
        storage.saveID("ACCESS_TOKEN", accessToken)
        storage.saveID("REFRESH_TOKEN", refreshToken)
    }
}

