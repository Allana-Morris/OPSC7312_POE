package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Register_Image_Upload : AppCompatActivity() {

    private var imageList: MutableList<Uri?> = MutableList(6) { null }
    private var currentImageViewIndex: Int = -1
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()  // Firebase Authentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_image_upload)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnContinue = findViewById<Button>(R.id.btnContinueImage)
        val imageViews = listOf(
            findViewById<ImageView>(R.id.imgUpload1),
            findViewById(R.id.imgUpload2),
            findViewById(R.id.imgUpload3),
            findViewById(R.id.imgUpload4),
            findViewById(R.id.imgUpload5),
            findViewById(R.id.imgUpload6)
        )

        // Authentication check before accessing Firestore or Storage
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            finish()  // Close activity if user is not logged in
            return
        }

        // Register image picker intent
        val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                selectedImageUri?.let { uri ->
                    if (currentImageViewIndex != -1) {
                        imageViews[currentImageViewIndex].setImageURI(uri) // Set image URI directly
                        imageList[currentImageViewIndex] = uri // Store the URI
                    } else {
                        Toast.makeText(this, "Failed to load selected image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listeners for image views to open the image picker
        for (i in imageViews.indices) {
            imageViews[i].setOnClickListener {
                currentImageViewIndex = i  // Track which ImageView was clicked
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                pickImage.launch(intent)  // Launch the image picker
            }
        }
        btnContinue.setOnClickListener {
            // Confirm the user is authenticated before proceeding
            val userId = currentUser.email  // Get user email directly from FirebaseAuth
            if (userId == null) {
                Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedImages = imageList.filterNotNull()
            if (selectedImages.isEmpty()) {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Handle image upload and save profile image URLs
            // ...
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, fileName: String, callback: (String?) -> Unit) {
        val storageRef = storage.child("profile_images/$fileName.jpeg")

        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseUpload", "Failed to upload image: ${exception.message}")
            callback(null)
        }
    }

    private fun saveProfileImageUrls(imageUrls: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser?.email == null) {
            Log.e("Firestore", "Error: User not authenticated")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Users")
            .whereEqualTo("email", currentUser.email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userDocuments = task.result
                    if (userDocuments != null && !userDocuments.isEmpty) {
                        val userDoc = userDocuments.documents[0]
                        userDoc.reference.update(mapOf("profileImageUrls" to imageUrls))
                            .addOnSuccessListener {
                                Log.d("Firestore", "Profile image URLs updated successfully")
                                startActivity(Intent(this, Register_Spotify_Link::class.java))
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating profile image URLs", e)
                                Toast.makeText(this, "Failed to save profile images. Try again.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e("Firestore", "No user document found for email: ${currentUser.email}")
                        Toast.makeText(this, "No user found with the given email.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Firestore", "Error querying user by email", task.exception)
                    Toast.makeText(this, "Error querying user by email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
