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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var sSecureStorage = SecureStorage(this)
        mAccessToken = loggedUser.user?.Name.toString()

        Toast.makeText(this, "Acces:" +mAccessToken, Toast.LENGTH_LONG).show()
        Toast.makeText(this, "other Acces:" + loggedUser.user?.SpotifyUserId, Toast.LENGTH_LONG).show()


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
                    updateUserProfile(
                        getString("name"),
                        getLong("age")?.toString() ?: "",
                        getString("pronoun"),
                        getString("profilePicUrl")
                    )

                    fetchTopGenre() // Fetch the top genre after updating the profile

                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileUI", "Error fetching user profile: ", exception)
            }
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

    private fun fetchTopGenre() {

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=50") // Fetch top artists
            .addHeader("Authorization", "Bearer ${loggedUser.user?.Name}") // Use the saved access token
            .build()


        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                Log.e("ProfileUI", "Error fetching top genres: ", e)
            }


            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        // Log the response code and body for debugging
                        val errorBody = response.body?.string() // Get the error body for more details
                        Log.e("ProfileUI", "Error fetching top genres fuck: ${response.code} - $errorBody")
                        throw IOException("Unexpected code $it")
                    }

                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val artists = jsonResponse.getJSONArray("items")
                    val genreMap = mutableMapOf<String, Int>() // To count genres

                    for (i in 0 until artists.length()) {
                        val artist = artists.getJSONObject(i)
                        val genres = artist.getJSONArray("genres")
                        for (j in 0 until genres.length()) {
                            val genre = genres.getString(j)
                            genreMap[genre] = genreMap.getOrDefault(genre, 0) + 1
                        }
                    }

                    // Find the genre with the highest count
                    val topGenre = genreMap.maxByOrNull { it.value }?.key
                    runOnUiThread {
                        // Update the UI with the top genre if needed
                        findViewById<TextView>(R.id.topGenreList).text = topGenre ?: "No top genre found"
                    }
                }
            }
        })
    }
}
