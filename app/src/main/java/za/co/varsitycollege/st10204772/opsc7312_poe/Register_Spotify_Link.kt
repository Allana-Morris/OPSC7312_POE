package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import com.squareup.picasso.Picasso

class Register_Spotify_Link : AppCompatActivity() {
    class Register_Spotify_Link : AppCompatActivity() {

        private val mOkHttpClient: OkHttpClient = OkHttpClient()
        private var mAccessToken: String? = null
        private var mCall: Call? = null
        val imageView: ImageView = findViewById(R.id.imgAccountPP)
        val textView: TextView = findViewById(R.id.txtSpotifyAccount)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_register_spotify_link)
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Spotify Authentication
            val btnSpotify = findViewById<Button>(R.id.btnspotifysearch)
            val btnsubmit = findViewById<Button>(R.id.btnContinueSpotify)
            val spotifyUserIdInput = findViewById<EditText>(R.id.edUsername)

            btnSpotify.setOnClickListener {
                val userId = spotifyUserIdInput.text.toString().trim()
                if (userId.isNotEmpty()) {
                    fetchSpotifyUserProfile(userId)
                } else {
                    textView.text = (R.string.spotify_userid_empty).toString()
                }
            }

            btnsubmit.setOnClickListener {

            }
        }

        // Fetch Spotify account details using the Spotify User ID
        private fun fetchSpotifyUserProfile(userId: String) {
            val request = Request.Builder()
                .url("https://api.spotify.com/v1/users/$userId")  // Spotify API endpoint for user profiles
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
                            val profileImages = jsonObject.optJSONArray("images")
                            val profileImageUrl = profileImages?.getJSONObject(0)?.getString("url") ?: ""

                            SpotifyData().spotifyId = userId
                            SpotifyData().displayName = displayName
                            SpotifyData().profpicurl = profileImageUrl.toUri()

                            // Update UI on the main thread
                            runOnUiThread {
                                textView.text = displayName
                                if (profileImageUrl.isNotEmpty()) {
                                    Picasso.get().load(profileImageUrl).into(imageView)
                                } else {
                                    imageView.setImageResource(R.drawable.profile_placeholder) // Default image if no profile pic
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
}
