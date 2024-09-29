package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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
    private var setSpotifyID : String? = null

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_spotify_link)
        val sStorage = SecureStorage(this)

        sStorage.saveID("eb9b8af983d94603adaa1d212cf58980", "CLIENT_ID")
        sStorage.saveID("myapp://callback", "REDIRECT_URI")
        sStorage.saveID( "44bdee846c714d22ad432b9b7cb1451b", "CLIENT_SECRET")
        sStorage.saveID( "905988466931-h3di4chs18somrfitguu3g95b0bf72sb.apps.googleusercontent.com", "GOOGLE_ID")

        val CLIENT_ID = sStorage.getID("CLIENT_ID")
        val REDIRECT_URI = sStorage.getID("REDIRECT_URI")

        var user = User()
        user.Email = "fixiongame@gmail.com"
        loggedUser.initializeUser(user)

        imageView = findViewById(R.id.imgAccountPP)
        textView = findViewById(R.id.txtSpotifyAccount)
        button = findViewById(R.id.btnContinueSpotify)
        button.isEnabled = false;


        //this is the continue button
        button.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()

            // Get the logged user's email
            val email = loggedUser.user?.Email

            // Set the Spotify ID to be stored
            if (email != null) {
                // Reference to the Users collection
                val usersCollection = firestore.collection("Users")

                // Query to find the user document where email matches
                usersCollection.whereEqualTo("email", email).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // Get the first document (assuming email is unique)
                            val userDocument = querySnapshot.documents[0]

                            // Update the document with the new field
                            userDocument.reference.update("spotifyId", setSpotifyID)
                                .addOnSuccessListener {
                                    val intent: Intent = Intent(
                                        this,
                                        ProfileUI::class.java
                                    )
                                    startActivity(intent)

                                    // Successfully updated
                                    Toast.makeText(this, "Spotify ID updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    // Handle the failure
                                    Toast.makeText(this, "Error updating Spotify ID: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // No matching user found
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure of the query
                        Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Handle the case where email is null
                Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
            }
        }

        val btnSpotify = findViewById<Button>(R.id.btnspotifysearch)



        btnSpotify.setOnClickListener {
            val builder =
                AuthorizationRequest.Builder(
                    CLIENT_ID,
                    AuthorizationResponse.Type.TOKEN,
                    REDIRECT_URI
                )
            builder.setScopes(arrayOf("streaming"))
            val request = builder.build()
            AuthorizationClient.openLoginInBrowser(this, request)

        }
    }

     override fun onNewIntent(intent: Intent) {

         if (intent != null) {
            super.onNewIntent(intent)
        }

        val uri: Uri? = intent?.data
        uri?.let {
            val response = AuthorizationResponse.fromUri(it)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Handle successful response
                    mAccessToken = response.accessToken
                    fetchSpotifyUserProfile()
                }
                AuthorizationResponse.Type.ERROR -> {
                    // Handle error response
                    textView.text = getString(R.string.spotify_user_fetch_fail)
                }
                else -> {
                    // Handle other cases
                }
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

                //we dont need email if its the same as the sign in anyfucking way
                //also removed the try and catch to see what an error was, yall can add it back if you want
                //fyi the error was the email



                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val displayName = jsonObject.getString("display_name")
                   // val email = jsonObject.getString("email")
                    val spotifyId = jsonObject.getString("id")
                    val profileImages = jsonObject.optJSONArray("images")
                    val profileImageUrl = if (profileImages != null && profileImages.length() > 0) {
                        profileImages.getJSONObject(0).optString("url", "")
                    }
                    else {
                    ""  // Default to empty string if no profile image is available
                    }
                    val apiHref = jsonObject.getString("external_urls").let {
                        Uri.parse(it)
                    }


                    // Update UI on the main thread
                    //if(User().Email.equals(email)) {
                        runOnUiThread {
                            textView.text = displayName
                            if (profileImageUrl.isNotEmpty()) {
                                Picasso.get().load(profileImageUrl).into(imageView)

                                SpotifyData().spotifyId = spotifyId
                                SpotifyData().apihref = apiHref
                                SpotifyData().profpicurl = Uri.parse(profileImageUrl)
                                SpotifyData().email = loggedUser.user?.Email.toString()
                                SpotifyData().displayName = displayName

                                setSpotifyID = spotifyId;

                                button.isEnabled = true;




                            } else {
                                imageView.setImageResource(R.drawable.profile_placeholder) // Default image if no profile pic
                            }
                        }
                    /*} else{
                        runOnUiThread {
                            textView.text =
                                getString(R.string.spotify_account_email_does_not_match_registered_email) // Email does not match spotify account
                        }
                    }*/
                 /*catch (e: JSONException) {
                    runOnUiThread {
                        textView.text = getString(R.string.spotify_user_parse_fail)
                    }
                }*/
            }
        })
    }

    override fun onDestroy() {
        mCall?.cancel() // Cancel any ongoing API requests
        super.onDestroy()
    }
}
