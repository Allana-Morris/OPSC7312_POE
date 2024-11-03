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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Chat : AppCompatActivity() {
    private lateinit var mesDao: messageDao
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        mesDao = roomDB.getDatabase(this)!!.messageDao()!!
        db = Firebase.firestore
        FirebaseApp.initializeApp(this)

        val sendBtn = findViewById<ImageButton>(R.id.imgBtnSend)
        val layout: LinearLayout = findViewById(R.id.vert_layout_chat)
        val contactName = intent.getStringExtra("contactName") ?: "Unknown"
        val contactID = intent.getStringExtra("contactID") ?: "Unknown"
        val docID = intent.getStringExtra("messageDocID") ?: "Unknown"

        val nameHeader = findViewById<TextView>(R.id.txtChatName)
        nameHeader.text = contactName

        setupBottomNavigation()

        // Load messages from local database first
        loadMessagesFromLocalDatabase(contactID, layout)

        // Sync with Firestore
        syncMessagesWithFirestore(contactID, docID, layout)

        // Send message on button click
        sendBtn.setOnClickListener {
            val messageText = findViewById<TextView>(R.id.txtInput).text.toString()
            if (messageText.isNotEmpty()) {
                sendMessageToFirestore(messageText, docID, contactID)
            } else {
                Toast.makeText(this, "Cannot send an empty message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessagesFromLocalDatabase(contactID: String, layout: LinearLayout) {
        // Load messages from Room database
        lifecycleScope.launch {
            val messages = withContext(Dispatchers.IO) {
                mesDao.getMessages(loggedUser.user?.Email, contactID)
            }
            messages?.forEach { Message ->
                displayMessage(Message!!.content ?: "", if (Message.fromUid == loggedUser.user?.Email) "sender" else "receiver", layout)
            }
        }
    }

    private fun sendMessageToFirestore(messageText: String, docID: String, contactID: String) {
        val newMessage = hashMapOf(
            "content" to messageText,
            "type" to "text",
            "uID" to loggedUser.user?.Email,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        // Display the message immediately
        val localMessage = Message().apply {
            fromUid = loggedUser.user?.Email
            toUid = contactID
            content = messageText
            timeStamp = System.currentTimeMillis().toString()
            type = "text"
        }

        // Display the message in the UI immediately
        displayMessage(messageText, "sender", findViewById(R.id.vert_layout_chat))

        lifecycleScope.launch(Dispatchers.IO) {
            // Insert into Room
            mesDao.insert(localMessage)
        }

        // Attempt to send to Firestore
        db.collection("message").document(docID).collection("msgList")
            .add(newMessage)
            .addOnSuccessListener {
                // Clear the input field after sending the message
                findViewById<TextView>(R.id.txtInput).text = ""
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun syncMessagesWithFirestore(contactID: String, docID: String, layout: LinearLayout) {
        db.collection("message").document(docID).collection("msgList")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Listen failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                lifecycleScope.launch {
                    snapshots?.documents?.forEach { msgDoc ->
                        val messageContent = msgDoc.getString("content") ?: ""
                        val messageType = msgDoc.getString("type") ?: "text"
                        val messageUid = msgDoc.getString("uID") ?: ""
                        val firestoreTimestamp = msgDoc.getTimestamp("timestamp")?.toDate()?.time

                        if (firestoreTimestamp != null) {
                            // Create the local message object
                            val newMessage = Message().apply {
                                fromUid = messageUid
                                toUid = if (messageUid == loggedUser.user?.Email) contactID else loggedUser.user?.Email
                                content = messageContent
                                timeStamp = firestoreTimestamp.toString()
                                type = messageType
                            }

                            // Insert into Room if not already present
                            withContext(Dispatchers.IO) {
                                if (mesDao.getMessages(newMessage.fromUid, newMessage.toUid)
                                        .none { it!!.content == messageContent && it.timeStamp == newMessage.timeStamp }
                                ) {
                                    mesDao.insert(newMessage)
                                    withContext(Dispatchers.Main) {
                                        displayMessage(
                                            messageContent,
                                            if (messageUid == loggedUser.user?.Email) "sender" else "receiver",
                                            layout
                                        )
                                    }
                                }
                            }
                        }
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
        val messageView: View
        val messageTextView: TextView

        if (messageType == "sender") {
            // Inflate sender layout
            messageView = LayoutInflater.from(this).inflate(R.layout.layout_chat_message_bubble_sender, layout, false)
            messageTextView = messageView.findViewById(R.id.txtBubbleSender)
        } else {
            // Inflate receiver layout
            messageView = LayoutInflater.from(this).inflate(R.layout.layout_chat_message_bubble, layout, false)
            messageTextView = messageView.findViewById(R.id.txtBubble)
        }

        messageTextView.text = messageText
        layout.addView(messageView)
    }

}
