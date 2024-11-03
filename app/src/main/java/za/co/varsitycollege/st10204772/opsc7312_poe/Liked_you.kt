package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class Liked_you : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liked_you)
        setupBottomNavigation()

        // Initialize layout and Firebase
        layout = findViewById(R.id.vert_layout_liked) // Ensure you have this LinearLayout in your XML
        db = FirebaseFirestore.getInstance()
        FirebaseApp.initializeApp(this)

        // Fetch users who liked you
        fetchLikedUsers()
    }

    private fun fetchLikedUsers() {
        db.collection("Users")
            .whereEqualTo("email", loggedUser.user?.Email)
            .get()
            .addOnSuccessListener { userDocuments ->
                userDocuments.forEach { userDocument ->
                    fetchLikedByDocuments(userDocument.id)
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching user: ${e.message}")
            }
    }

    private fun fetchLikedByDocuments(userId: String) {
        Toast.makeText(this, "Fetching liked users...", Toast.LENGTH_SHORT).show()

        db.collection("Users")
            .document(userId)
            .collection("liked_by")
            .get()
            .addOnSuccessListener { likedByDocuments ->
                if (likedByDocuments.isEmpty) {
                    showToast("No one liked you :(")
                } else {
                    likedByDocuments.forEach { likedByDocument ->
                        val uid = likedByDocument.getString("uid")
                        if (uid != null) {
                            // Now fetch the user details from the Users collection using uid
                            fetchUserDetails(uid)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching liked_by: ${e.message}")
            }
    }

    private fun fetchUserDetails(uid: String) {
        db.collection("Users")
            .whereEqualTo("email", uid) // Assuming `uid` is a field in the Users collection
            .get()
            .addOnSuccessListener { userDocuments ->
                if (userDocuments.isEmpty) {
                    showToast("No user found with uid: $uid")
                } else {
                    userDocuments.forEach { userDocument ->
                        val name = userDocument.getString("name")
                        val profileImgList = userDocument.get("profileImageUrls") as? List<String> ?: emptyList()
                        val imageUrl = profileImgList.getOrNull(0) // Get the first image URL or null if not available

                        if (name != null && imageUrl != null) {
                            addUserToLayout(name, uid, imageUrl) // Pass the name and image URL to the layout function
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching user details: ${e.message}")
            }
    }


    private fun addUserToLayout(name: String, uid: String, profileImg: String) {
        // Inflate the user layout
        val userView = LayoutInflater.from(this).inflate(R.layout.layout_liked_you, layout, false)
        userView.findViewById<TextView>(R.id.txtLikedName).text = name

        // Load the profile image into the ImageView using Glide
        val imageView = userView.findViewById<ImageView>(R.id.imgLikedYou)
        Glide.with(this)
            .load(profileImg) // Load the image URL
            .into(imageView) // Set the image view to display the image

        // Set up the profile view button
        userView.findViewById<FloatingActionButton>(R.id.viewLikedProfileBtn).apply {
            setOnClickListener {
                startActivity(Intent(this@Liked_you, MatchUI::class.java).apply {
                    putExtra("userId", uid) // Pass the uid to the MatchUI if needed
                })
            }
        }

        // Set up the add contact button
        userView.findViewById<FloatingActionButton>(R.id.contactLikedBtn).apply {
            setOnClickListener { handleAddContact(uid) }
        }

        // Set up the reject button
        userView.findViewById<FloatingActionButton>(R.id.rejectBtn).apply {
            setOnClickListener { deleteLikedContact(uid) }
        }

        // Add the user view to the layout
        layout.addView(userView)
    }


    private fun handleAddContact(toUid: String) {
        val newMessage = hashMapOf(
            "fromUid" to loggedUser.user?.Email,
            "toUid" to toUid
        )

        db.collection("message")
            .add(newMessage)
            .addOnSuccessListener { documentReference ->
                showToast("Contact added! Redirecting...")
                deleteLikedContact(toUid)
                startActivity(Intent(this, Contact::class.java).apply {
                    putExtra("messageDocId", documentReference.id)
                })
            }
            .addOnFailureListener { e ->
                showToast("Error adding contact: ${e.message}")
            }
    }

    private fun deleteLikedContact(toUid: String) {
        db.collection("Users")
            .whereEqualTo("email", loggedUser.user?.Email)
            .get()
            .addOnSuccessListener { userDocuments ->
                userDocuments.forEach { userDocument ->
                    db.collection("Users")
                        .document(userDocument.id)
                        .collection("liked_by")
                        .whereEqualTo("uid", toUid)
                        .get()
                        .addOnSuccessListener { likedByDocuments ->
                            likedByDocuments.forEach { likedByDocument ->
                                db.collection("Users")
                                    .document(userDocument.id)
                                    .collection("liked_by")
                                    .document(likedByDocument.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        showToast("Liked contact removed.")
                                        refreshLikedUsers()
                                    }
                                    .addOnFailureListener { e ->
                                        showToast("Error removing liked contact: ${e.message}")
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            showToast("Error fetching liked contacts: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching user: ${e.message}")
            }
    }

    private fun refreshLikedUsers() {
        layout.removeAllViews() // Clear current views
        fetchLikedUsers() // Fetch and display updated liked users
    }

    private fun setupBottomNavigation() {
        val navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Liked_You)
        navbar.selectedItemId = R.id.nav_like
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
