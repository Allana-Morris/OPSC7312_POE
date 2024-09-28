package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
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
import com.google.android.material.bottomnavigation.BottomNavigationView

class MatchUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var spotifyAccessToken: String? = null
    private val TAG = "MatchUI"
    private lateinit var currentUserTopSongs: List<String>
    // Variables to hold filter data
    private var selectedGender: String? = null
    private var selectedGenre: String? = null

    // Register a result launcher for the filter activity
    private val filterResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedGender = data?.getStringExtra("selectedGender")
            selectedGenre = data?.getStringExtra("selectedGenre")

            // Apply filtering based on the selected gender and genre
            fetchFilteredProfiles()
        }
    }

    @SuppressLint("MissingInflatedId")
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
        // Adjust Firestore query to include gender and genre filters
        selectedGenre?.let {
            db.collection("users")
                .whereEqualTo("gender", selectedGender)
                .whereArrayContains("favoriteGenres", it)
                .get()
                .addOnSuccessListener { documents ->
                    // Handle profile loading and display
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
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
            .url("https://api.spotify.com/v1/me/top/tracks?limit=3")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Error fetching top songs: $e")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let {
                    val jsonResponse = JSONObject(it.string())
                    val topTracks = jsonResponse.getJSONArray("items")

                    // Extract the first song's name and artist name
                    val song = topTracks.getJSONObject(0) // Getting the first song
                    val songName = song.getString("name")
                    val artistName = song.getJSONArray("artists")
                        .getJSONObject(0).getString("name") // First artist in the list

                    // Update the UI with song and artist names
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvSongName).text = songName
                        findViewById<TextView>(R.id.tvArtistName).text = artistName
                    }

                    // Store current user's top songs for matching later
                    currentUserTopSongs = List(topTracks.length()) { i ->
                        topTracks.getJSONObject(i).getString("name")
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

                    // Update the UI with new user's details
                    findViewById<TextView>(R.id.tvName).text = "$userName, $userAge"
                    findViewById<TextView>(R.id.tvPronouns).text = userPronouns
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting next user: ", exception)
            }
    }

    private fun checkForMatch() {
        // Fetch the top 3 songs of the displayed user and compare them with current user's songs
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

                        // Extract top 3 songs of the viewed user
                        val viewedUserTopSongs = List(topTracks.length()) { i ->
                            topTracks.getJSONObject(i).getString("name")
                        }

                        // Check if the current user and the viewed user have any common songs
                        val commonSongs = currentUserTopSongs.intersect(viewedUserTopSongs)

                        runOnUiThread {
                            if (commonSongs.isNotEmpty()) {
                                // If there's a match, show a message
                                Toast.makeText(this@MatchUI, "It's a match! Common songs: $commonSongs", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@MatchUI, "No match found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }

        var navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Match)

        navbar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_match -> {
                    startActivity(Intent(this, MatchUI::class.java))
                    true
                }
                R.id.nav_like -> {
                    startActivity(Intent(this, Liked_you::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, Contact::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileUI::class.java))
                    true
                }
                else -> false
            }
        }
    }
}