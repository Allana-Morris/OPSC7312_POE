package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Register_Spotify_Link : AppCompatActivity() {

    private val CLIENT_ID = "42c1126b97d64d1b8a85c6099ae180d4" // Replace with your Spotify Client ID
    private val REDIRECT_URI = "musicmatch://spotify-callback"
    private val REQUEST_CODE = 1337  // Arbitrary request code for identifying the response

    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mAccessToken: String? = null
    private var mCall: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_spotify_link)

        Log.d("SpotifyAuth", "Register_Spotify_Link: onCreate() called")

        // Start Spotify authentication when the activity is created
        authenticateSpotify()

        // Initialize WebView (if necessary for additional usage)
        setupWebView()

        val btnSpotify = findViewById<Button>(R.id.btnspotifysearch)
        val btnSubmit = findViewById<Button>(R.id.btnContinueSpotify)
        val spotifyUserIdInput = findViewById<EditText>(R.id.edUsername)

        val displayNameTextView = findViewById<TextView>(R.id.txtSpotifyAccount)
        val profileImageView = findViewById<ImageView>(R.id.imgAccountPP)

        btnSpotify.setOnClickListener {
            val userId = spotifyUserIdInput.text.toString().trim()
            if (userId.isNotEmpty()) {
                if (mAccessToken == null) {
                    // Show error if access token is missing
                    displayNameTextView.text = "Access token is missing"
                    return@setOnClickListener
                }

                // Call the Spotify API with the token
                fetchSpotifyUserProfile(userId, displayNameTextView, profileImageView)
            } else {
                displayNameTextView.text = getString(R.string.spotify_userid_empty)
            }
        }

        btnSubmit.setOnClickListener {
            // Navigate to MatchUI after user data is fetched
            startActivity(Intent(this, MatchUI::class.java))
        }
    }

    // Start Spotify authentication flow
    private fun authenticateSpotify() {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )

        builder.setScopes(arrayOf("user-read-private", "user-read-email"))  // Request the necessary Spotify scopes
        val request = builder.build()

        val intent = AuthorizationClient.createLoginActivityIntent(this, request)
        startActivityForResult(intent, REQUEST_CODE)
    }


    // Setup WebView configuration
    private fun setupWebView() {
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true  // Enable JavaScript for the WebView

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("SpotifyAuth", "WebView finished loading: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("SpotifyAuth", "WebView error: ${error?.description}")
                // Handle WebView errors if needed
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            Log.d("SpotifyAuth", "onActivityResult - Response received: ${response.type}")
            Log.d("SpotifyAuth", "Result Code: $resultCode")
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Access token successfully received
                    mAccessToken = response.accessToken
                    Log.d("SpotifyAuth", "Access token: $mAccessToken")
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("SpotifyAuth", "Error: ${response.error}")
                }
                else -> {
                    Log.d("SpotifyAuth", "Unknown response")
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("ACCESS_TOKEN", mAccessToken)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mAccessToken = savedInstanceState.getString("ACCESS_TOKEN")
        Log.d("SpotifyAuth", "Restored access token: $mAccessToken")
    }

    // Fetch Spotify account details using the Spotify User ID
    private fun fetchSpotifyUserProfile(userId: String, displayNameTextView: TextView, profileImageView: ImageView) {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/users/$userId")  // Spotify API endpoint for user profiles
            .addHeader("Authorization", "Bearer $mAccessToken")  // Use access token
            .build()

        mCall = mOkHttpClient.newCall(request)

        mCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    displayNameTextView.text = getString(R.string.spotify_user_fetch_fail)
                    Log.e("SpotifyError", "Failed to fetch user: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        displayNameTextView.text = getString(R.string.spotify_user_fetch_fail)
                    }
                    return
                }

                try {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val displayName = jsonObject.getString("display_name")
                    val profileImages = jsonObject.optJSONArray("images")
                    val profileImageUrl = profileImages?.getJSONObject(0)?.getString("url") ?: ""

                    // Update UI with the fetched details on the main thread
                    runOnUiThread {
                        displayNameTextView.text = displayName
                        if (profileImageUrl.isNotEmpty()) {
                            Picasso.get().load(profileImageUrl).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.profile_placeholder)  // Default image
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        displayNameTextView.text = getString(R.string.spotify_user_parse_fail)
                        Log.e("SpotifyError", "JSON parsing error: ${e.message}")
                    }
                }
            }
        })
    }

}
