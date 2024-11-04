package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentSnapshot
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ProfileUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mAccessToken: String? = null // Ensure this is set with the access token you saved earlier
    private lateinit var tvTopGenre: TextView
    private lateinit var tvTopArtist: TextView
    private lateinit var tvTopSong: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvTopGenre = findViewById<TextView>(R.id.topGenreList)
        tvTopArtist = findViewById<TextView>(R.id.topArtistList)
        tvTopSong = findViewById<TextView>(R.id.topSongList)

        mAccessToken = loggedUser.user?.userToken.toString()

        db = FirebaseFirestore.getInstance()

        // Fetch user profile data
        fetchUserProfile()

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

    private fun fetchUserProfile() {
        val userEmail = loggedUser.user?.Email ?: run {
            return
        }

        // Query the Users collection for the logged-in user
        db.collection("Users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    return@addOnSuccessListener
                }

                // Assuming only one user document matches the email
                documents.documents[0].apply {
                    val name = getString("name")
                    val age = getLong("age")?.toString() ?: ""
                    val pronouns = getString("pronoun")

                    // Retrieve the list of uploaded images by casting the result as List<String>
                    val imageUrls = this["profileImageUrls"] as? List<String>
                    val profilePicUrl = imageUrls?.firstOrNull()  // Get the first image as the profile picture

                    // Fetch top artists, genres, and songs
                    val topArtists = getList("topArtists")
                    val topGenres = getList("topGenres")
                    val topSongs = getList("topSongs")

                    updateUserProfile(name, age, pronouns, profilePicUrl)

                    // Do something with the top artists, genres, and songs
                    tvTopArtist.text = topArtists.joinToString("\n")
                    tvTopGenre.text = topGenres.joinToString("\n")
                    tvTopSong.text = topSongs.joinToString("\n")
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
