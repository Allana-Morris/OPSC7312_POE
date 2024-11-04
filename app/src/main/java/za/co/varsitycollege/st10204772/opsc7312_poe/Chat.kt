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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

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
        val docID = intent.getStringExtra("docId") ?: "Unknown"

        val nameHeader = findViewById<TextView>(R.id.txtChatName)
        nameHeader.text = contactName

        setupBottomNavigation()

        // Load messages from local database first
        loadMessagesFromLocalDatabase(contactID, layout)

        // Sync with Firestore
        syncMessagesWithFirestore(contactID, docID, layout)


        Toast.makeText(this, "docID:  ${docID}  ", Toast.LENGTH_LONG).show()


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
                mesDao.getMessages(contactID, contactID)
            }
            messages?.forEach { Message ->
                displayMessage(Message!!.content ?: "", if (Message.fromUid == loggedUser.user?.Email) "sender" else "receiver", layout)
            }
        }
    }

    private fun sendMessageToFirestore(messageText: String, docID: String, contactID: String) {
        // Create the Firestore message data
        val newMessage = hashMapOf(
            "content" to messageText,
            "type" to "text",
            "uID" to loggedUser.user?.Email,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )


        lifecycleScope.launch(Dispatchers.IO) {

            val contactToken = getContactFCMToken(contactID)

            // Generate a unique message ID
            val newMessageId = generateUniqueMessageId(mesDao)  // Ensure generateUniqueMessageId is defined and returns a unique string

            val localMessage = Message().apply {
                id = newMessageId  // Set the unique ID here
                fromUid = loggedUser.user?.Email
                toUid = contactID
                content = messageText
                timeStamp = System.currentTimeMillis().toString()
                type = "text"
                fcmToken = contactToken
            }

            // Insert the message into Room
            mesDao.insert(localMessage)

            // Switch back to the main thread to update the UI


            // Attempt to send to Firestore
            db.collection("message").document(docID).collection("msgList")
                .document(newMessageId)  // Use newMessageId here for the Firestore document ID
                .set(newMessage)
                .addOnSuccessListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        displayMessage(messageText, "sender", findViewById(R.id.vert_layout_chat))
                    }
                    // Clear the input field after sending the message
                    findViewById<TextView>(R.id.txtInput).text = ""
                }
                .addOnFailureListener { e ->


                }

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
                        val messageId = msgDoc.id
                        val messageContent = msgDoc.getString("content") ?: ""
                        val messageType = msgDoc.getString("type") ?: "text"
                        val messageUid = msgDoc.getString("uID") ?: ""
                        val firestoreTimestamp = msgDoc.getTimestamp("timestamp")?.toDate()?.time

                        // Only proceed if the timestamp is not null
                        if (firestoreTimestamp != null) {
                            val newMessage = Message().apply {
                                id = messageId
                                fromUid = messageUid
                                toUid = if (messageUid == loggedUser.user?.Email) contactID else loggedUser.user?.Email
                                content = messageContent
                                timeStamp = firestoreTimestamp.toString()
                                type = messageType
                            }

                            // Check if the message already exists in Room
                            withContext(Dispatchers.IO) {
                                val exists = mesDao.checkMessageIdExists(messageId)
                                if (!exists) {
                                    // Insert message into Room
                                    mesDao.insert(newMessage)

                                    // Update the UI on the main thread
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

    suspend fun generateUniqueMessageId(mesDao: messageDao): String {
        var uniqueId: String
        do {
            // Generate a random UUID
            uniqueId = UUID.randomUUID().toString()
        } while (mesDao.checkMessageIdExists(uniqueId))  // Repeat if ID already exists in the database
        return uniqueId
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

    private suspend fun getContactFCMToken(contactID: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val contactDoc = db.collection("contacts").document(contactID).get().await()
                contactDoc.getString("fcmToken")
            } catch (e: Exception) {
                null // Handle any exception (e.g., contact not found) gracefully
            }
        }
    }

}
