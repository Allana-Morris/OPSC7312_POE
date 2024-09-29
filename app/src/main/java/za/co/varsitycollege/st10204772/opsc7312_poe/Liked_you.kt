package za.co.varsitycollege.st10204772.opsc7312_poe

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore

class Liked_you : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liked_you)

        val layout: LinearLayout = findViewById(R.id.vert_layout_liked)

        var liked = mutableListOf<String>()  // To store message docID and contactID

        val db = Firebase.firestore
        FirebaseApp.initializeApp(this)

        // Query the Users collection to get the current user's document
        db.collection("Users")
            .whereEqualTo("email", loggedUser.user?.Email)
            .get()
            .addOnSuccessListener { userDocuments ->
                for (userDocument in userDocuments) {
                    // Access the liked_by sub-collection
                    db.collection("Users")
                        .document(userDocument.id) // Get the user document ID
                        .collection("liked_by")
                        .get()
                        .addOnSuccessListener { likedByDocuments ->
                            for (likedByDocument in likedByDocuments) {
                                // Extract the uid field from each document in the liked_by sub-collection
                                val uid = likedByDocument.getString("uid")
                                uid?.let { liked.add(it) } // Add uid to the liked list
                            }

                            // Optionally, you can display or process the liked list here
                            if (liked.isNotEmpty()) {
                                // Display liked users
                                for (userId in liked) {
                                    // Create a view for each liked user (assuming a TextView)
                                    val textView = LayoutInflater.from(this).inflate(R.layout.layout_liked_you, layout, false) // Inflate your user item layout
                                    textView.findViewById<TextView>(R.id.txtLikedName).text = userId // Assuming you have a TextView to display the uid
                                    layout.addView(textView) // Add the user view to the layout
                                }
                            } else {
                                // Handle no liked users found
                                Toast.makeText(this, "No one liked you :(", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error fetching liked_by: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
