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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.REDIRECT_URI3

class ProfileUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var roomDb: roomDB // Add RoomDB instance
    private lateinit var tvTopGenre: TextView
    private lateinit var tvTopArtist: TextView
    private lateinit var tvTopSong: TextView
    private lateinit var settings: Button
    private var lUser: User = User()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvTopGenre = findViewById(R.id.topGenreList)
        tvTopArtist = findViewById(R.id.topArtistList)
        tvTopSong = findViewById(R.id.topSongList)
        settings = findViewById(R.id.btnSettings)
        db = FirebaseFirestore.getInstance()
        roomDb = roomDB.getDatabase(this) // Initialize Room database

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userID", null)

        if (!(currentUserId.isNullOrEmpty())) {
            Log.d(TAG, "There's an ID")
            fetchUserData(currentUserId)
        } else {
            Log.e(TAG, "No ID")
        }

        settings.setOnClickListener {
            val intent = Intent(this@ProfileUI, SettingsUI::class.java)
            startActivity(intent)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        val usersCollection = db.collection("Users")

        usersCollection.whereEqualTo("email", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    if (userDocument != null) {
                        lUser = userDocument.toObject<User>()!!
                        loggedUser.user = lUser
                        // Save to Room
                        saveUserToRoom(lUser)

                        // Fetch user profile
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
                // Fallback to Room database
                fetchUserFromRoom(userId)
            }
    }

    private fun saveUserToRoom(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if user already exists in Room database by email
            val existingUser = roomDb.localUserDao().getUserByEmail(user.Email)

            val localUser = LocalUser(
                name = user.Name,
                age = user.Age,
                pronoun = user.Pronoun,
                email = user.Email
            )

            if (existingUser != null) {
                // Update existing user (replace is handled by insert)
                localUser.id = existingUser.id // Assuming LocalUser has an 'id' field to preserve the existing ID
                roomDb.localUserDao().insert(localUser) // This will update the existing user
            } else {
                // Insert new user
                roomDb.localUserDao().insert(localUser)
            }
        }
    }


    private fun fetchUserFromRoom(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val localUser = roomDb.localUserDao().getUserById(userId.toInt()) // Ensure userId is convertible to Int
            if (localUser != null) {
                updateUserProfile(localUser.name, localUser.age.toString(), localUser.pronoun, null)
                // Show "No internet connection" for top genres, artists, songs
                runOnUiThread {
                    tvTopArtist.text = "No internet connection"
                    tvTopGenre.text = "No internet connection"
                    tvTopSong.text = "No internet connection"
                }
            } else {
                Log.d(TAG, "No user found in Room")
            }
        }
    }

    private fun fetchUserProfile(userEmail: String) {
        db.collection("Users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("ProfileUI", "No user found with email: $userEmail")
                    return@addOnSuccessListener
                }

                documents.documents[0].apply {
                    val name = getString("name") ?: "Unknown"
                    val age = getLong("age")?.toString() ?: "N/A"
                    val pronouns = getString("pronoun") ?: "Not specified"
                    val imageUrls = get("profileImageUrls") as? List<String> ?: emptyList()
                    val profilePicUrl = imageUrls.firstOrNull() ?: "No profile picture available"
                    val topArtists = get("topArtists") as? List<String> ?: emptyList()
                    val topGenres = get("topGenres") as? List<String> ?: emptyList()
                    val topSongs = get("topSongs") as? List<String> ?: emptyList()

                    updateUserProfile(name, age, pronouns, profilePicUrl)

                    // Show top artists, genres, and songs
                    runOnUiThread {
                        tvTopArtist.text = if (topArtists.isNotEmpty()) topArtists.joinToString("\n") { it } else "No internet connection"
                        tvTopGenre.text = if (topGenres.isNotEmpty()) topGenres.joinToString("\n") { it } else "No internet connection"
                        tvTopSong.text = if (topSongs.isNotEmpty()) topSongs.joinToString("\n") { it } else "No internet connection"
                    }
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
}
