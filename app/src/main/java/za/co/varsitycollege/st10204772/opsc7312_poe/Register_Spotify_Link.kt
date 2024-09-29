package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID
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

    private val redirectUri= "myapp://callback"
    private val authRequestCode = 1337
    private val scopes = "user-read-private user-read-email user-top-read"
    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mAccessToken: String? = null
    private var mCall: Call? = null

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private val sStorage = SecureStorage(this)
    private val CLIENT_ID = sStorage.getID("CLIENT_ID")
    private val REDIRECT_URI = sStorage.getID("REDIRECT_URI")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_spotify_link)

        imageView = findViewById(R.id.imgAccountPP)
        textView = findViewById(R.id.txtSpotifyAccount)
        val btnSpotify = findViewById<Button>(R.id.btnspotifysearch)



        btnSpotify.setOnClickListener {
            val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.CODE, REDIRECT_URI)
            builder.setScopes(arrayOf(scopes))
            val request = builder.build()
            AuthorizationClient.openLoginActivity(this, authRequestCode, request)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == authRequestCode) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    mAccessToken = response.accessToken
                    fetchSpotifyUserProfile()
                }
                AuthorizationResponse.Type.ERROR -> {
                    textView.text = getString(R.string.spotify_user_fetch_fail)
                }
                else -> {}
            }
        }
    }

    // Fetch Spotify account details
    private fun fetchSpotifyUserProfile() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")  // Spotify API endpoint for current user profile
            .addHeader("Authorization", "Bearer $mAccessToken")  // Use access token
            .build()

        mCall = mOkHttpClient.newCall(request)

        mCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    textView.text = getString(R.string.spotify_user_fetch_fail)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val displayName = jsonObject.getString("display_name")
                    val email = jsonObject.getString("email")
                    val spotifyId = jsonObject.getString("id")
                    val profileImages = jsonObject.optJSONArray("images")
                    val profileImageUrl = profileImages?.getJSONObject(0)?.getString("url") ?: ""
                    val apiHref = jsonObject.getString("external_urls").let {
                        Uri.parse(it)
                    }

                    // Update UI on the main thread
                    if(User().Email.equals(email)) {
                        runOnUiThread {
                            textView.text = displayName
                            if (profileImageUrl.isNotEmpty()) {
                                Picasso.get().load(profileImageUrl).into(imageView)

                                SpotifyData().spotifyId = spotifyId
                                SpotifyData().apihref = apiHref
                                SpotifyData().profpicurl = Uri.parse(profileImageUrl)
                                SpotifyData().email = email
                                SpotifyData().displayName = displayName

                            } else {
                                imageView.setImageResource(R.drawable.profile_placeholder) // Default image if no profile pic
                            }
                        }
                    } else{
                        runOnUiThread {
                            textView.text =
                                getString(R.string.spotify_account_email_does_not_match_registered_email) // Email does not match spotify account
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        textView.text = getString(R.string.spotify_user_parse_fail)
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        mCall?.cancel() // Cancel any ongoing API requests
        super.onDestroy()
    }
}
