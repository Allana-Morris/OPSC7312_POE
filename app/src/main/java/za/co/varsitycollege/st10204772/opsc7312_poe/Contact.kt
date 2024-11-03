package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Contact : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var contactDao: ContactDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)
        val layout: LinearLayout = findViewById(R.id.vert_layout_contact)

        db = Firebase.firestore
        FirebaseApp.initializeApp(this)

        contactDao = roomDB.getDatabase(this)!!.contactDao()!!

        setupBottomNavigation()

        // Sync contacts from Firestore to Room
        syncContactsFromFirestore()

        // Load contacts from Room to display
        loadContactsFromRoom(layout)
    }

    private fun setupBottomNavigation() {
        val navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Profile)
        navbar.selectedItemId = R.id.nav_chat
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

    private fun syncContactsFromFirestore() {
        // Clear existing contacts to avoid duplicates
        lifecycleScope.launch(Dispatchers.IO) {
            contactDao.clearContacts()

            // Query Firestore to find contacts where the logged-in user is either fromUid or toUid
            db.collection("message")
                .whereEqualTo("fromUid", loggedUser.user?.Email)
                .get()
                .addOnSuccessListener { fromDocuments ->
                    for (document in fromDocuments) {
                        val receiverID = document.getString("toUid")
                        receiverID?.let { saveContactToLocalDB(it, document.id) } // Pass document ID
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@Contact, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            // Now query for messages where the logged-in user is the receiver
            db.collection("message")
                .whereEqualTo("toUid", loggedUser.user?.Email)
                .get()
                .addOnSuccessListener { toDocuments ->
                    for (document in toDocuments) {
                        val senderID = document.getString("fromUid")
                        senderID?.let { saveContactToLocalDB(it, document.id) } // Pass document ID
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@Contact, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveContactToLocalDB(contactID: String, messageDocId: String) {
        db.collection("Users")
            .whereEqualTo("email", contactID)
            .get()
            .addOnSuccessListener { userDocs ->
                if (!userDocs.isEmpty) {
                    val document = userDocs.documents[0]
                    val userName = document.getString("name") ?: "Unknown"

                    // Save contact to local Room database
                    val newContact = Cont().apply {
                        email = contactID
                        name = userName
                        lastMessageId = messageDocId // Set lastMessageId to the message document ID
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        contactDao.insertContact(newContact)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@Contact, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadContactsFromRoom(layout: LinearLayout) {
        lifecycleScope.launch {
            val contacts = withContext(Dispatchers.IO) { contactDao.getAllContacts() }
            contacts.forEach { cont ->
                displayContact(cont, layout)
            }
        }
    }

    private fun displayContact(contact: Cont, layout: LinearLayout) {
        val inflatedView = LayoutInflater.from(this).inflate(R.layout.layout_contact_listing, layout, false)

        val contactNameTextView = inflatedView.findViewById<TextView>(R.id.txtContactName)
        contactNameTextView.text = contact.name

        inflatedView.setOnClickListener {
            val intent = Intent(this@Contact, Chat::class.java)
            intent.putExtra("contactName", contact.name)
            intent.putExtra("contactID", contact.email)
            intent.putExtra("docId", contact.lastMessageId)

            startActivity(intent)
        }

        layout.addView(inflatedView)
    }
}
