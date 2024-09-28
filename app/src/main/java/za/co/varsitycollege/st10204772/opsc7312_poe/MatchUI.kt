package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Response
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import javax.security.auth.callback.Callback

class MatchUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var spotifyAccessToken: String? = null
    private val TAG = "MatchUI"
    // Store user's top songs, artists, and genres
    private lateinit var currentUserTopSongs: List<String>
    private lateinit var currentUserTopArtists: List<String>
    private lateinit var currentUserTopGenres: List<String>
    // Variables to hold filter data
    private var selectedGender: String? = null
    private var selectedGenre: String? = null
    private var selectedLocation: String? = null

    // Register a result launcher for the filter activity
    private val filterResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedGender = data?.getStringExtra("selectedGender")
            selectedGenre = data?.getStringExtra("selectedGenre")
            selectedLocation = data?.getStringExtra("selectedLocation")  // Get the selected location

            if (selectedGender == null || selectedGenre == null || selectedLocation == null) {
                Log.e(TAG, "Invalid filter data received: gender=$selectedGender, genre=$selectedGenre, location=$selectedLocation")
                Toast.makeText(this, "Invalid filters selected", Toast.LENGTH_SHORT).show()
            } else {
                // Apply filtering based on the selected gender, genre, and location
                fetchFilteredProfiles()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_ui)

        // Trigger FilterActivity
        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            filterResultLauncher.launch(intent)
        }

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance()

        // Fetch user data (Name, Age, Pronouns) from Firestore
        fetchUserDetails()

        // Fetch top 3 songs from Spotify
        spotifyAccessToken?.let { token ->
            fetchTopSongsFromSpotify(token)
        }

        // Set an onClickListener on the profile picture to navigate to ProfileUI
        val profilePic = findViewById<ImageView>(R.id.tvProfilePic)
        profilePic.setOnClickListener {
            val intent = Intent(this, ProfileUI::class.java)
            // Pass any additional data if needed (e.g., user ID)
            startActivity(intent)
        }

        // Set onClickListeners for Floating Action Buttons
        findViewById<FloatingActionButton>(R.id.fab_nope).setOnClickListener {
            // Handle Nope: Fetch and display the next user
            fetchNextUser()
        }

        findViewById<FloatingActionButton>(R.id.fab_like).setOnClickListener {
            // Handle Like: Check for match based on top 3 songs
            checkForMatch()
        }
    }

    // Fetch profiles based on selected filters
    private fun fetchFilteredProfiles() {
        val query = db.collection("users")

        // Apply gender filter
        selectedGender?.let { gender ->
            query.whereEqualTo("gender", gender)
        }

        // Apply genre filter
        selectedGenre?.let { genre ->
            query.whereArrayContains("favoriteGenres", genre)
        }

        // Apply location filter
        selectedLocation?.let { location ->
            query.whereEqualTo("location", location)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No profiles found with selected filters", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "No profiles found")
                } else {
                    for (document in documents) {
                        Log.d(TAG, "Found profile: ${document.data}")
                        // Handle displaying of profiles here
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error fetching profiles with filters: ", exception)
                Toast.makeText(this, "Error loading filtered profiles", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserDetails() {
        // Assuming Firestore stores user's name, age, and pronouns
        db.collection("users").document("user_id").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name")
                    val userAge = document.getLong("age")?.toString() ?: ""
                    val userPronouns = document.getString("pronouns")

                    // Display user data in respective fields
                    findViewById<TextView>(R.id.tvName).text = "$userName, $userAge"
                    findViewById<TextView>(R.id.tvPronouns).text = userPronouns
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting user details: ", exception)
            }
    }

    private fun fetchTopSongsFromSpotify(accessToken: String) {
        val request = okhttp3.Request.Builder()
            .url("https://api.spotify.com/v1/me/top/tracks?limit=1") // Fetch only the top song
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Error fetching top song: $e")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let {
                    val jsonResponse = JSONObject(it.string())
                    val topTrack = jsonResponse.getJSONArray("items").getJSONObject(0)

                    // Extract song name, artist name, and album cover
                    val songName = topTrack.getString("name")
                    val artistName = topTrack.getJSONArray("artists").getJSONObject(0).getString("name")
                    val albumCoverUrl = topTrack.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")

                    // Update UI with the fetched details
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvSongName).text = songName
                        findViewById<TextView>(R.id.tvArtistName).text = artistName

                        // Load the album cover into ImageView
                        val albumCoverImageView = findViewById<ImageView>(R.id.tvAlbumCover)
                        Glide.with(this@MatchUI)
                            .load(albumCoverUrl)
                            .into(albumCoverImageView)
                    }
                }
            }
        })
    }


    private fun fetchNextUser() {
        // Fetch and display the next user from Firestore
        db.collection("users").document("next_user_id").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name")
                    val userAge = document.getLong("age")?.toString() ?: ""
                    val userPronouns = document.getString("pronouns")
                    val profileImageUrls = document.get("profileImageUrls") as? List<String>  // Fetch image URLs list
                    val topSongName = document.getString("topSongName") // Assuming Firestore has the top song name
                    val topSongAlbumCoverUrl = document.getString("topSongAlbumCoverUrl") // Assuming Firestore has the album cover URL

                    // Update the UI with new user's details
                    findViewById<TextView>(R.id.tvName).text = "$userName, $userAge"
                    findViewById<TextView>(R.id.tvPronouns).text = userPronouns
                    findViewById<TextView>(R.id.tvSongName).text = topSongName ?: "No song available"

                    // Load the first profile image using Glide, if available
                    val profilePicImageView = findViewById<ImageView>(R.id.tvProfilePic)
                    if (!profileImageUrls.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrls[0])  // Load the first image in the list
                            .into(profilePicImageView)
                    } else {
                        // Set a placeholder image if no image is available
                        profilePicImageView.setImageResource(R.drawable.ic_profile)
                    }

                    // Load the album cover using Glide, if available
                    val albumCoverImageView = findViewById<ImageView>(R.id.tvAlbumCover)
                    if (!topSongAlbumCoverUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(topSongAlbumCoverUrl)  // Load the album cover image
                            .into(albumCoverImageView)
                    } else {
                        albumCoverImageView.setImageResource(R.drawable.albumimage) // Placeholder for album cover
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting next user: ", exception)
            }
    }

    private fun checkForMatch() {
        spotifyAccessToken?.let { token ->
            val request = okhttp3.Request.Builder()
                .url("https://api.spotify.com/v1/me/top/tracks?limit=3")
                .addHeader("Authorization", "Bearer $token")
                .build()

            OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e(TAG, "Error fetching top songs: $e")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.body?.let {
                        val jsonResponse = JSONObject(it.string())
                        val topTracks = jsonResponse.getJSONArray("items")

                        // Extract top songs and artists of the viewed user
                        val viewedUserTopSongs = List(topTracks.length()) { i ->
                            topTracks.getJSONObject(i).getString("name")
                        }
                        val viewedUserTopArtists = List(topTracks.length()) { i ->
                            topTracks.getJSONObject(i)
                                .getJSONArray("artists").getJSONObject(0).getString("name")
                        }

                        // Fetch genres for the viewed user's artists and store them
                        val viewedUserTopGenres = mutableSetOf<String>()

                        val genreFetchers = viewedUserTopArtists.map { artistName ->
                            fetchArtistGenres(token, artistName, viewedUserTopGenres)
                        }

                        // Wait for all genre fetching requests to complete
                        genreFetchers.forEach { it.join() }

                        runOnUiThread {
                            // Check for matches in songs, artists, and genres
                            val commonSongs = currentUserTopSongs.intersect(viewedUserTopSongs)
                            val commonArtists = currentUserTopArtists.intersect(viewedUserTopArtists)
                            val commonGenres = currentUserTopGenres.intersect(viewedUserTopGenres)

                            if (commonSongs.isNotEmpty() || commonArtists.isNotEmpty() || commonGenres.isNotEmpty()) {
                                // If there's a match, show a message
                                Toast.makeText(
                                    this@MatchUI,
                                    "It's a match! Common songs: $commonSongs, artists: $commonArtists, genres: $commonGenres",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(this@MatchUI, "No match found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
    }

    // Helper function to fetch artist genres from Spotify API
    private fun fetchArtistGenres(accessToken: String, artistName: String, viewedUserTopGenres: MutableSet<String>): Thread {
        val thread = Thread {
            val request = okhttp3.Request.Builder()
                .url("https://api.spotify.com/v1/search?q=$artistName&type=artist&limit=1")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e(TAG, "Error fetching artist genres: $e")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.body?.let {
                        val jsonResponse = JSONObject(it.string())
                        val artists = jsonResponse.getJSONObject("artists").getJSONArray("items")

                        if (artists.length() > 0) {
                            val artist = artists.getJSONObject(0)
                            val genres = artist.getJSONArray("genres")
                            for (i in 0 until genres.length()) {
                                viewedUserTopGenres.add(genres.getString(i))  // Add each genre to the set
                            }
                        }
                    }
                }
            })
        }
        thread.start()
        return thread
    }
}