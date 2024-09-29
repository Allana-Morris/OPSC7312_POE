package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        layout = findViewById(R.id.vert_layout_liked)
        db = FirebaseFirestore.getInstance()
        FirebaseApp.initializeApp(this)

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
                        val name = likedByDocument.getString("name")

                        if (uid != null && name != null) {
                            addUserToLayout(name, uid)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching liked_by: ${e.message}")
            }
    }

    private fun addUserToLayout(name: String, uid: String) {
        val userView = LayoutInflater.from(this).inflate(R.layout.layout_liked_you, layout, false)
        userView.findViewById<TextView>(R.id.txtLikedName).text = name

        val viewProfile = userView.findViewById<FloatingActionButton>(R.id.viewLikedProfileBtn)
        viewProfile.setOnClickListener { Toast.makeText(this, "name: " + uid, Toast.LENGTH_SHORT).show() }

        val addContact = userView.findViewById<FloatingActionButton>(R.id.contactLikedBtn)
        addContact.setOnClickListener { handleAddContact(uid) }

        val reject = userView.findViewById<FloatingActionButton>(R.id.rejectBtn)
        reject.setOnClickListener { deleteLikedContact(uid) }

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
                                        intent = Intent(this, Liked_you::class.java)
                                        startActivity(intent);
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
