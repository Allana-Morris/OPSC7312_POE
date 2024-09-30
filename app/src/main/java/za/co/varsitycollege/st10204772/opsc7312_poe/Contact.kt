package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class Contact : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)
        val layout: LinearLayout = findViewById(R.id.vert_layout_contact)

        val db = Firebase.firestore
        FirebaseApp.initializeApp(this)

        var messageDocs = mutableListOf<Pair<String, String>>()  // To store message docID and contactID
        setupBottomNavigation()




// Query where loggedUser's cellNo matches "fromUid"
        db.collection("message")
            .whereEqualTo("fromUid", loggedUser.user?.Email)
            .get()
            .addOnSuccessListener { fromDocuments ->
                for (document in fromDocuments) {
                    val receiverID = document.getString("toUid") // Add the receiver
                    receiverID?.let { messageDocs.add(Pair(document.id, it)) }
                }

                // Now query where loggedUser's cellNo matches "toUid"
                db.collection("message")
                    .whereEqualTo("toUid", loggedUser.user?.Email)
                    .get()
                    .addOnSuccessListener { toDocuments ->
                        for (document in toDocuments) {
                            val senderID = document.getString("fromUid") // Add the sender
                            senderID?.let  { messageDocs.add(Pair(document.id, it)) }
                        }

                        // Show contacts in the set after both queries complete
                        if (messageDocs.isNotEmpty()) {
                            Toast.makeText(this, messageDocs.toString(), Toast.LENGTH_SHORT).show()

                            showContacts(messageDocs, db, layout)

                        } else {
                            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

    fun showContacts(messageDocs: List<Pair<String, String>>, db: FirebaseFirestore, layout: LinearLayout) {
        for ((messageDocID, contactID) in messageDocs) {
            db.collection("Users")
                .whereEqualTo("email", contactID)
                .get()
                .addOnSuccessListener { userDocs ->
                    if (!userDocs.isEmpty) {
                        val document = userDocs.documents[0]

                        val userName = document.getString("name")

                        val inflatedView = LayoutInflater.from(this@Contact)
                            .inflate(R.layout.layout_contact_listing, layout, false)

                        val contactNameTextView = inflatedView.findViewById<TextView>(R.id.txtContactName)
                        contactNameTextView.text = userName ?: "Unknown"

                        inflatedView.setOnClickListener {
                            val intent = Intent(this@Contact, Chat::class.java)
                            intent.putExtra("contactName", userName)
                            intent.putExtra("contactID", contactID)
                            intent.putExtra("messageDocID", messageDocID)  // Pass the message document ID

                            startActivity(intent)
                        }

                        layout.addView(inflatedView)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@Contact, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
