package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.Auth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.OkHttpClient
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.REDIRECT_URI3

class ProfileUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var tvTopGenre: TextView
    private lateinit var tvTopArtist: TextView
    private lateinit var tvTopSong: TextView
    private lateinit var settings: Button
    private var lUser: User = User()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvTopGenre = findViewById<TextView>(R.id.topGenreList)
        tvTopArtist = findViewById<TextView>(R.id.topArtistList)
        tvTopSong = findViewById<TextView>(R.id.topSongList)
        settings = findViewById<Button>(R.id.btnSettings)
        db = FirebaseFirestore.getInstance()

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userID", null)

        if (!(currentUserId.isNullOrEmpty())) {
            Log.d(TAG, "Theres an ID")
            fetchUserData(currentUserId)
        } else {
            Log.e(TAG, "No ID")
        }

        settings.setOnClickListener {
            val intent = Intent(this@ProfileUI, SettingsUI::class.java)
            startActivity(intent)
        }
          // Optional: Handle back navigation here if needed
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Profile)
        navbar.selectedItemId = R.id.nav_profile
        navbar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_match -> startActivity(Intent(this, MatchUI::class.java))
                R.id.nav_like -> startActivity(Intent(this, Liked_you::class.java))
                R.id.nav_chat -> startActivity(Intent(this, Contact::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileUI::class.java))
                else -> false
            }
            true
        }
    }

    private fun fetchUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("Users")

        // Query to find the user document by userId
        usersCollection.whereEqualTo("email", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    if (userDocument != null) {
                        lUser = userDocument.toObject<User>()!! // Assuming User data class exists
                        loggedUser.user = lUser
                        fetchUserProfile(loggedUser.user?.Email.toString())
                    } else {
                        Log.d(TAG, "No such document")
                    }
                } else {
                    Log.d(TAG, "User not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Get failed with ", exception)
            }
    }

    private fun fetchUserProfile(userEmail: String) {

        // Query the Users collection for the logged-in user
        db.collection("Users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("ProfileUI", "No user found with email: $userEmail")
                    return@addOnSuccessListener
                }

                // Assuming only one user document matches the email
                documents.documents[0].apply {
                    val name = getString("name") ?: "Unknown"
                    val age = getLong("age")?.toString() ?: "N/A"
                    val pronouns = getString("pronoun") ?: "Not specified"

                    // Retrieve the list of uploaded images by casting the result as List<String>
                    val imageUrls = get("profileImageUrls") as? List<String> ?: emptyList()
                    val profilePicUrl = imageUrls.firstOrNull() ?: "No profile picture available"

                    // Fetch top artists, genres, and songs
                    val topArtists = get("topArtists") as? List<String> ?: emptyList()
                    val topGenres = get("topGenres") as? List<String> ?: emptyList()
                    val topSongs = get("topSongs") as? List<String> ?: emptyList()

                    updateUserProfile(name, age, pronouns, profilePicUrl)

                    // Do something with the top artists, genres, and songs
                    tvTopArtist.text = topArtists.joinToString("\n") { it }
                    tvTopGenre.text = topGenres.joinToString("\n") { it }
                    tvTopSong.text = topSongs.joinToString("\n") { it }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileUI", "Error fetching user profile: ", exception)
            }
    }

    // Helper method to convert Firestore array to List<String>
    private fun DocumentSnapshot.getList(field: String): List<String> {
        return this.get(field) as? List<String> ?: emptyList()
    }

    private fun updateUserProfile(name: String?, age: String, pronouns: String?, profilePicUrl: String?) {
        findViewById<TextView>(R.id.tvProfileName).text = "$name, $age"
        findViewById<TextView>(R.id.tvProfilePronouns).text = pronouns

        profilePicUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_profile)
                .into(findViewById(R.id.profileImageView))
        }
    }
}
