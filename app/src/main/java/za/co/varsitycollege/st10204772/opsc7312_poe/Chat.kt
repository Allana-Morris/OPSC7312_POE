package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.internal.CallbackExecutor.executorService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


private lateinit var mesDao: messageDao


class Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        mesDao = roomDB.getDatabase(this)?.messageDao()!!


        val sendBtn = findViewById<ImageButton>(R.id.imgBtnSend)
        insertTestUser();


        val layout: LinearLayout = findViewById(R.id.vert_layout_chat)
        val contactName = intent.getStringExtra("contactName") ?: "Unknown"
        val contactID = intent.getStringExtra("contactID") ?: "Unknown"
        val docID = intent.getStringExtra("messageDocID") ?: "Unknown"

        setupBottomNavigation()


        val nameHeader = findViewById<TextView>(R.id.txtChatName)

        nameHeader.text = contactName;

        val db = Firebase.firestore
        FirebaseApp.initializeApp(this)

        db.collection("message")
            .whereEqualTo("fromUid", loggedUser.user?.Email)
            .whereEqualTo("toUid", contactID)
            .get()
            .addOnSuccessListener { fromResults ->
                if (fromResults.isEmpty) {
                    // Check for documents where loggedUser is the recipient and contactID is the sender
                    db.collection("message")
                        .whereEqualTo("fromUid", contactID)
                        .whereEqualTo("toUid", loggedUser.user?.Email)
                        .get()
                        .addOnSuccessListener { toResults ->
                            if (toResults.isEmpty) {
                                Toast.makeText(this, "No messages found", Toast.LENGTH_SHORT).show()
                            } else {
                                val document = toResults.documents[0]
                                val messageDocId = document.id
                                // Listen for new messages in real-time
                                listenForMessages(messageDocId, db, layout)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val document = fromResults.documents[0]
                    val messageDocId = document.id
                    // Listen for new messages in real-time
                    listenForMessages(messageDocId, db, layout)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }


        sendBtn.setOnClickListener {
            val messageText = findViewById<TextView>(R.id.txtInput).text.toString()

            if (messageText.isNotEmpty()) {
                // Create a new message map
                val newMessage = hashMapOf(
                    "content" to messageText,
                    "type" to "text",   // Setting type to 'text'
                    "uID" to loggedUser.user?.Email,  // User's UID (cellNo)
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp() // Add timestamp
                )

                // Assuming you're using the document ID already retrieved earlier
                val messageDocId = docID // You need to get the appropriate document ID for the chat

                // Save the message to the msgList sub-collection
                db.collection("message").document(messageDocId).collection("msgList")
                    .add(newMessage)
                    .addOnSuccessListener {
                        // Clear the input field after sending the message
                        findViewById<TextView>(R.id.txtInput).text = ""
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private val processedMessages = mutableSetOf<String>()


    private fun listenForMessages(messageDocId: String, db: FirebaseFirestore, layout: LinearLayout) {
        db.collection("message").document(messageDocId).collection("msgList")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Listen failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                for (docChange in snapshots!!.documentChanges) {
                    val msgDoc = docChange.document
                    val messageId = msgDoc.id

                    // Check if the message has already been processed
                    if (processedMessages.contains(messageId)) {
                        continue
                    }

                    // Add the message ID to the set to mark it as processed
                    processedMessages.add(messageId)

                    val messageText = msgDoc.getString("content") ?: ""
                    val messageType = msgDoc.getString("type") ?: "text" // Assuming 'text' type
                    val messageUid = msgDoc.getString("uID") // Get the Uid of the message sender

                    // Check if the message Uid matches the logged-in user's cellNo
                    if (messageUid == loggedUser.user?.Email) {
                        displayMessage(messageText, "sender", layout) // Inflate sender layout
                    } else {
                        displayMessage(messageText, "receiver", layout) // Inflate receiver layout
                    }
                }
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




    private fun displayMessage(messageText: String, messageType: String, layout: LinearLayout) {
        var messageView: View?
        var messageTextView : TextView?

        if (messageType == "sender") {
            messageView = LayoutInflater.from(this@Chat).inflate(R.layout.layout_chat_message_bubble_sender, layout, false)
            messageTextView = messageView.findViewById<TextView>(R.id.txtBubbleSender)

        } else {
            messageView = LayoutInflater.from(this@Chat).inflate(R.layout.layout_chat_message_bubble, layout, false)
            messageTextView = messageView.findViewById<TextView>(R.id.txtBubble)

        }

        messageTextView.text = messageText

        // Add the message view to the chat layout
        layout.addView(messageView)
    }

    private fun insertTestUser() {
        executorService().execute {
            // Check if the test user 'admin' already exists in the db

                // Insert the test user
                val testmessage = message()
                testmessage.id = 6;
                testmessage.content = "im killing myself"
                testmessage.type = "text"
                testmessage.toUid = "d"
                testmessage.fromUid = "peenits"
                testmessage.timestamp = "12 0 cock";

                mesDao.insert(testmessage);

        }
    }
}
/*for ( int in 1..30)
       {
           val inflatedView = LayoutInflater.from(this@Chat)
               .inflate(R.layout.layout_chat_message_bubble, layout, false)

           layout.addView(inflatedView)
       }*/