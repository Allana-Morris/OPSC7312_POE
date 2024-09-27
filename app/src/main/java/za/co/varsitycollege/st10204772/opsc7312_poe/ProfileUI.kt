package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ProfileUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = FirebaseFirestore.getInstance()

        fetchUserProfile()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Optional: Handle back navigation here if needed
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    fun fetchUserProfile() {
        val userId = "user_id" // Replace with actual user ID logic

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("name")
                    val userAge = document.getLong("age")?.toString() ?: ""
                    val userPronouns = document.getString("pronouns")
                    val profilePicUrl = document.getString("profilePicUrl")

                    findViewById<TextView>(R.id.tvProfileName).text = "$userName, $userAge"
                    findViewById<TextView>(R.id.tvProfilePronouns).text = userPronouns

                    // Load profile picture using Glide or Picasso
                    profilePicUrl?.let { url ->
                        Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.ic_profile)
                            .into(findViewById(R.id.profileImageView))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching profile: ", exception)
            }
    }
}