package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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
import org.json.JSONObject
import java.io.IOException

class Register_Spotify_Link : AppCompatActivity() {

    private val scopes =
        "user-read-private user-read-email user-top-read" // Add necessary scopes here
    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mAccessToken: String = ""
    private var mCall: Call? = null
    private var setSpotifyID: String? = null
    private val CLIENT_ID = ClientID.CLIENT_ID
    private val REDIRECT_URI = ClientID.REDIRECT_URI

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_spotify_link)

        imageView = findViewById(R.id.imgAccountPP)
        textView = findViewById(R.id.txtSpotifyAccount)
        button = findViewById(R.id.btnContinueSpotify)
        button.isEnabled = false;

        val builder =
            AuthorizationRequest.Builder(
                CLIENT_ID,
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
            )
        // Split the scopes string into an array and set it
        builder.setScopes(arrayOf(*scopes.split(" ").toTypedArray()))
        val request = builder.build()
        AuthorizationClient.clearCookies(this)
        AuthorizationClient.openLoginInBrowser(this, request)

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
                                    val intent = Intent(this, ProfileUI::class.java)
                                    startActivity(intent)

                                    // Successfully updated
                                    Toast.makeText(this, "Spotify ID updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    // Handle the failure
                                    Toast.makeText(this, "Error updating Spotify ID: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("FirestoreError", "Error updating Spotify ID: ${e.message}")
                                }
                        } else {
                            // No matching user found
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure of the query
                        Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("FirestoreError", "Error fetching user: ${e.message}")
                    }
            } else {
                // Handle the case where email is null
                Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri: Uri? = intent?.data
        uri?.let {
            val response = AuthorizationResponse.fromUri(it)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    // Handle successful response
                    mAccessToken = response.accessToken
                    loggedUser.user?.Name = mAccessToken.toString();
                    saveAccessToken(mAccessToken)  // Save the access token
                    fetchSpotifyUserProfile()
                }
                AuthorizationResponse.Type.ERROR -> {
                    // Handle error response
                    textView.text = getString(R.string.spotify_user_fetch_fail)
                }

                else -> {
                    // Handle other cases
                    Log.e(this@Register_Spotify_Link.toString(), "Access Token Issue")
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

                val responseBody = response.body?.string() ?: ""
                Log.d("Response Body", responseBody)
                val jsonObject = JSONObject(responseBody)

                val spotifyid = jsonObject.getString("id")
                val displayName = jsonObject.getString("display_name")
                val profileImages = jsonObject.optJSONArray("images")
                val profileImageUrl = if (profileImages != null && profileImages.length() > 0) {
                    profileImages.getJSONObject(0).optString("url", "")
                } else {
                    ""  // Default to empty string if no profile image is available
                }
                val apiHref = jsonObject.getJSONObject("external_urls").getString("spotify")

                runOnUiThread {
                    textView.text = displayName
                    if (spotifyid.isNotEmpty()) {
                        if (profileImageUrl.isNotEmpty()) {
                            Picasso.get().load(profileImageUrl).into(imageView)
                        }
                        else {
                            imageView.setImageResource(R.drawable.profile_placeholder) // Default image if no profile pic
                        }
                        SpotifyData().spotifyId = spotifyid
                        setSpotifyID = spotifyid
                        Log.d("SpotifyID", "Spotify ID to store: $setSpotifyID")
                        SpotifyData().apihref = Uri.parse(apiHref)
                        SpotifyData().profpicurl = Uri.parse(profileImageUrl)
                        SpotifyData().email = loggedUser.user?.Email.toString()
                        SpotifyData().displayName = displayName


                    } else {
                        Log.d("SpotifyID", "Failed to get Spotify ID")
                    }
                    button.isEnabled = true;
                }
                fetchTopGenre()
                fetchTopSongs()
                fetchTopArtists()

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
