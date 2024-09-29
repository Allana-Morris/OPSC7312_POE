package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        db = FirebaseFirestore.getInstance()

        fetchUserProfile()
        //this caused an error somehow so i killed him
        /* ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
*/
        // Optional: Handle back navigation here if needed
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Profile)

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


    fun fetchUserProfile() {
        val userEmail = loggedUser.user?.Email // Ensure this is the email, not an ID

        if (userEmail != null) {
            // Query the Users collection where the email matches the logged-in user's email
            db.collection("Users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        // Assuming only one user document matches the email
                        val document = documents.documents[0]

                        // Retrieve user data
                        val userName = document.getString("name")
                        val userAge = document.getLong("age")?.toString() ?: ""
                        val userPronouns = document.getString("pronoun")
                        val profilePicUrl = document.getString("profilePicUrl")

                        // Update UI elements with the retrieved data
                        findViewById<TextView>(R.id.tvProfileName).text = "$userName, $userAge"
                        findViewById<TextView>(R.id.tvProfilePronouns).text = userPronouns

                        // Load profile picture using Glide or Picasso
                        profilePicUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile)
                                .into(findViewById(R.id.profileImageView))
                        }
                    } else {
                        Log.e(TAG, "No user found with the provided email.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching profile: ", exception)
                }
        } else {
            Log.e(TAG, "Logged user email is null.")
        }
    }
}

