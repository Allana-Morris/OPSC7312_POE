package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.squareup.picasso.Picasso
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class Register_Spotify_Link : AppCompatActivity() {

    private val scopes =
        "user-read-private user-read-email user-top-read" // Add necessary scopes here
    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mAccessToken: String = ""
    private var mCall: Call? = null
    private var setSpotifyID: String? = null
    private lateinit var codeVerifier: String

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var fcmToken: String

    private val sharedPrefs: SharedPreferences by lazy {
        getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_spotify_link)
        startSpotifyAuthentication()
        imageView = findViewById(R.id.imgAccountPP)
        textView = findViewById(R.id.txtSpotifyAccount)
        button = findViewById(R.id.btnContinueSpotify)
        button.isEnabled = false;
        startSpotifyAuthentication()

        button.setOnClickListener {
            getFcmToken()
            RegistrationFunction()
        }
    }


    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            fcmToken = task.result ?: ""
            User().FCMToken = fcmToken
            Log.d(TAG, "FCM Token: $fcmToken")
        }
    }

    private fun RegistrationFunction() {
        // Assuming the registration logic is handled here and was successful
        val firestore = FirebaseFirestore.getInstance()
        val email = loggedUser.user?.Email

        if (email != null) {
            val usersCollection = firestore.collection("Users")
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]
                        userDocument.reference.update(
                            "spotifyId", setSpotifyID,
                            "FCMToken", fcmToken  // Store the FCM token
                        ).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Spotify ID updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating Spotify ID: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSpotifyAuthentication() {
        val (verifier, challenge) = ClientID.generateCodeVerifierAndChallenge()
        codeVerifier = verifier // Save code verifier for later use

        val authorizationUrl = "${ClientID.AUTH_URL}?response_type=code" +
                "&client_id=${ClientID.CLIENT_ID}" +
                "&redirect_uri=${ClientID.REDIRECT_URI}" +
                "&code_challenge=$challenge" +
                "&code_challenge_method=S256"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check if the activity was started with a redirect
        val intent = intent
        val uri: Uri? = intent.data
        if (uri != null && uri.toString().startsWith(ClientID.REDIRECT_URI)) {
            val code = uri.getQueryParameter("code")

            if (code != null) {
                exchangeCodeForToken(code, codeVerifier) // Ensure this line is correct
            }
        }
    }

    private fun exchangeCodeForToken(code: String, codeVerifier: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", ClientID.REDIRECT_URI)
            .add("client_id", ClientID.CLIENT_ID)
            .add("code_verifier", codeVerifier)
            .build()

        val request = Request.Builder()
            .url(ClientID.TOKEN_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val tokenResponse = Gson().fromJson(responseData, ClientID.TokenResponse::class.java)

                    val mAccessToken = tokenResponse.access_token
                    saveAccessToken(mAccessToken) // Save the access token
                    SessionManager(this@Register_Spotify_Link).createLoginSession(User().Email, mAccessToken)
                    fetchSpotifyUserProfile()
                } else {
                    // Handle error response
                    textView.text = getString(R.string.spotify_user_fetch_fail)
                }
            }
        })
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

                val responseBody = response.body?.string() ?: ""
                Log.d("Response Body", responseBody)
                val jsonObject = JSONObject(responseBody)
                val displayName = jsonObject.getString("display_name")
                val profileImages = jsonObject.optJSONArray("images")
                val spotifyid = jsonObject.getString("id")
                val profileImageUrl = if (profileImages != null && profileImages.length() > 0) {
                    profileImages.getJSONObject(0).optString("url", "")
                } else {
                    ""  // Default to empty string if no profile image is available
                }
                val apiHref = jsonObject.getJSONObject("external_urls").getString("spotify")

                runOnUiThread {
                    textView.text = displayName
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(imageView)

                        SpotifyData().spotifyId = spotifyid
                        SpotifyData().apihref = Uri.parse(apiHref)
                        SpotifyData().profpicurl = Uri.parse(profileImageUrl)
                        SpotifyData().email = loggedUser.user?.Email.toString()
                        SpotifyData().displayName = displayName

                        fetchTopGenre()
                        fetchTopSongs()
                        fetchTopArtists()


                        button.isEnabled = true;

                    } else {
                        imageView.setImageResource(R.drawable.profile_placeholder) // Default image if no profile pic
                    }
                }
            }
        })
    }

    private fun saveAccessToken(token: String?) {
        if (token != null) {
            loggedUser.user?.Name = token
        }
    }

    private fun fetchTopGenre() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=50") // Fetch top artists
            .addHeader("Authorization", "Bearer $mAccessToken") // Use access token
            .build()

        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = response.body?.string()
                        return
                    }

                    val responseBody = response.body?.string() ?: ""
                    Log.d("Response Body", responseBody)
                    val jsonObject = JSONObject(responseBody)
                    val artists = jsonObject.getJSONArray("items")
                    val genreMap = mutableMapOf<String, Int>() // To count genres

                    // Count occurrences of each genre
                    for (i in 0 until artists.length()) {
                        val artist = artists.getJSONObject(i)
                        val genres = artist.getJSONArray("genres")
                        for (j in 0 until genres.length()) {
                            val genre = genres.getString(j)
                            genreMap[genre] = genreMap.getOrDefault(genre, 0) + 1
                        }
                    }

                    // Sort genres by count and retrieve the top 3
                    val topGenres = genreMap.toList()
                        .sortedByDescending { it.second }
                        .take(3)
                        .map { it.first } // Get only the genre names

                    // Store the top genres in Firestore
                    storeTopGenresInFirestore(topGenres)
                }
            }
        })
    }

    // Store top genres in Firestore
    private fun storeTopGenresInFirestore(topGenres: List<String>) {
        val firestore = FirebaseFirestore.getInstance()

        // Get the logged user's email
        val email = loggedUser.user?.Email

        if (email != null) {
            // Reference to the Users collection
            val usersCollection = firestore.collection("Users")

            // Query to find the user document where email matches
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]

                        // Update the document with the top genres
                        userDocument.reference.update("topGenres", topGenres)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top genres updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top genres: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchTopSongs() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/tracks?limit=3") // Fetch top tracks
            .addHeader("Authorization", "Bearer $mAccessToken") // Use access token
            .build()

        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                //F*cking Failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = response.body?.string()
                        return
                    }

                    val responseBody = response.body?.string() ?: ""
                    Log.d("Response Body", responseBody)
                    val jsonObject = JSONObject(responseBody)
                    val songs = jsonObject.getJSONArray("items")
                    val topSongs = mutableListOf<String>() // To hold top song names
                    val SongArtistName = mutableListOf<String>()
                    val albumArt = mutableListOf<String>()

                    // Retrieve the top 3 song names
                    for (i in 0 until songs.length()) {
                        val song = songs.getJSONObject(i)
                        val songName = song.getString("name")

                        val artistArray = song.getJSONArray("artists")
                        val artistName = artistArray.getJSONObject(0).getString("name")

                        // Get the album artwork URL
                        val album = song.getJSONObject("album")
                        val images = album.getJSONArray("images")
                        val artworkUrl = images.getJSONObject(0)
                            .getString("url") // Usually, index 0 is the highest resolution

                        topSongs.add(songName)
                        SongArtistName.add(artistName)
                        albumArt.add(artworkUrl)
                    }
                    // Store the top songs in Firestore
                    storeTopSongsInFirestore(topSongs, SongArtistName, albumArt)
                }
            }
        })
    }

    // Store top songs in Firestore
    private fun storeTopSongsInFirestore(
        topSongs: List<String>,
        songartist: List<String>,
        albumart: List<String>
    ) {
        val firestore = FirebaseFirestore.getInstance()

        // Get the logged user's email
        val email = loggedUser.user?.Email

        if (email != null) {
            // Reference to the Users collection
            val usersCollection = firestore.collection("Users")

            // Query to find the user document where email matches
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]

                        // Update the document with the top songs
                        userDocument.reference.update("topSongs", topSongs)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top songs updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top songs: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        userDocument.reference.update("songArtist", songartist)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top songs artists updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top songs artists: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        userDocument.reference.update("albumArt", albumart)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top songs updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top songs: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchTopArtists() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=3") // Fetch top artists
            .addHeader("Authorization", "Bearer $mAccessToken") // Use access token
            .build()

        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                runOnUiThread {
                    Toast.makeText(
                        this@Register_Spotify_Link,
                        "Error fetching top artists: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = response.body?.string()
                        runOnUiThread {
                            Toast.makeText(
                                this@Register_Spotify_Link,
                                "Failed to fetch top artists: $errorBody",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return
                    }

                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val artists = jsonResponse.getJSONArray("items")

                    val topArtists = mutableListOf<String>() // Store top artist names
                    for (i in 0 until artists.length()) {
                        val artist = artists.getJSONObject(i)
                        val artistName = artist.getString("name") // Get artist name
                        topArtists.add(artistName) // Add to list
                    }

                    // Store top songs and artists in Firestore
                    storeTopArtistsInFirestore(topArtists)
                }
            }
        })
    }

    private fun storeTopArtistsInFirestore(topArtists: List<String>) {
        val firestore = FirebaseFirestore.getInstance()
        val email = loggedUser.user?.Email

        if (email != null) {
            // Reference to the Users collection
            val usersCollection = firestore.collection("Users")

            // Query to find the user document where email matches
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]

                        // Update the document with the top artists
                        userDocument.reference.update("topArtists", topArtists)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top artists updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top artists: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        mCall?.cancel() // Cancel any ongoing API requests
        super.onDestroy()
    }
}
