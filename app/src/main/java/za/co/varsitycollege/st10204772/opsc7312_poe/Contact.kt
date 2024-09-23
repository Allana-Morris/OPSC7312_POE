package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        var loggedUser = User("ed sheeran", "0987654321");

        val db = Firebase.firestore
        FirebaseApp.initializeApp(this)

        val contactSet = mutableSetOf<String>()

// Query where loggedUser's cellNo matches "fromUid"
        db.collection("message")
            .whereEqualTo("fromUid", loggedUser.cellNo)
            .get()
            .addOnSuccessListener { fromDocuments ->
                for (document in fromDocuments) {
                    val receiverID = document.getString("toUid") // Add the receiver
                    receiverID?.let { contactSet.add(it) }
                }

                // Now query where loggedUser's cellNo matches "toUid"
                db.collection("message")
                    .whereEqualTo("toUid", loggedUser.cellNo)
                    .get()
                    .addOnSuccessListener { toDocuments ->
                        for (document in toDocuments) {
                            val senderID = document.getString("fromUid") // Add the sender
                            senderID?.let { contactSet.add(it) }
                        }

                        // Show contacts in the set after both queries complete
                        if (contactSet.isNotEmpty()) {
                            Toast.makeText(this, contactSet.toString(), Toast.LENGTH_SHORT).show()

                            showContacts(contactSet, db, layout)

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

    fun showContacts(contactSet: MutableSet<String>, db: FirebaseFirestore, layout: LinearLayout) {

        for (contactID in contactSet) {
            // Query where the "cellNo" field equals contactID
            db.collection("Users")
                .whereEqualTo("cellNo", contactID)  // Query by cellNo field
                .get()
                .addOnSuccessListener { userDocs ->
                    if (!userDocs.isEmpty) {
                        val document = userDocs.documents[0]  // Get the first matching document
                        val userName = document.getString("Name")  // Get the "Name" field

                        // Inflate a new contact item into the layout
                        val inflatedView = LayoutInflater.from(this@Contact)
                            .inflate(R.layout.layout_contact_listing, layout, false)

                        // Assuming you have a TextView in your layout to set the contact's name
                        val contactNameTextView =
                            inflatedView.findViewById<TextView>(R.id.txtContactName)
                        contactNameTextView.text = userName ?: "Unknown"  // Set the name or "Unknown"

                        // Set an OnClickListener for each inflated view
                        inflatedView.setOnClickListener {
                            // Handle click event for this particular contact
                            Toast.makeText(this@Contact, "Clicked on: $userName", Toast.LENGTH_SHORT).show()

                            // You can start a new activity or perform another action here
                            // Example: Start a chat activity
                            val intent = Intent(this@Contact, Chat::class.java)
                            intent.putExtra("contactName", userName)
                            intent.putExtra("contactID", contactID)
                            startActivity(intent)
                        }

                        // Add the inflated view to the layout
                        layout.addView(inflatedView)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@Contact, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }



}
/*  for ( int in 1..30)
       {
           val inflatedView = LayoutInflater.from(this@Contact)
               .inflate(R.layout.layout_contact_listing, layout, false)

           layout.addView(inflatedView)
       }*/