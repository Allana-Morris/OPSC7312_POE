package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class Register_Image_Upload : AppCompatActivity() {

    private var imageList: MutableList<Bitmap?> = MutableList(6) { null } // Six placeholders for six images
    private var currentImageViewIndex: Int = -1
    private val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_image_upload)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnContinue = findViewById<Button>(R.id.btnContinueImage)
        val imageViews = listOf<ImageView>(
            findViewById(R.id.imgUpload1),
            findViewById(R.id.imgUpload2),
            findViewById(R.id.imgUpload3),
            findViewById(R.id.imgUpload4),
            findViewById(R.id.imgUpload5),
            findViewById(R.id.imgUpload6)
        )

        val pickImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val selectedImageUri: Uri? = result.data?.data
                    selectedImageUri?.let { uri ->
                        val bitmap = uriToBitmap(uri)
                        if (currentImageViewIndex != -1 && bitmap != null) {
                            imageViews[currentImageViewIndex].setImageBitmap(bitmap)
                            imageList[currentImageViewIndex] = bitmap
                        } else {
                            Toast.makeText(this, "Failed to load selected image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
                }
            }

        for (i in imageViews.indices) {
            imageViews[i].setOnClickListener {
                currentImageViewIndex = i  // Keep track of which ImageView is clicked
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                pickImage.launch(intent)  // Launch the image picker
            }
        }

        btnContinue.setOnClickListener {
            // Ensure the user is authenticated before uploading
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedImages = imageList.filterNotNull() // Get only selected images (non-null)

            if (selectedImages.isEmpty()) {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imageUrls = mutableListOf<String>()
            var imagesUploaded = 0

            selectedImages.forEachIndexed { index, bitmap ->
                uploadImageToFirebaseStorage(bitmap, "image_$index") { downloadUrl ->
                    if (downloadUrl != null) {
                        imageUrls.add(downloadUrl)
                        imagesUploaded++

                        // Proceed to the next activity once all images are uploaded
                        if (imagesUploaded == selectedImages.size) {
                            saveProfileImageUrls(userId, imageUrls)
                            startActivity(Intent(this, Register_Spotify_Link::class.java))
                        }
                    } else {
                        Toast.makeText(this, "Failed to upload image $index", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, fileName: String, callback: (String?) -> Unit) {
        val storageRef = storage.child("profile_images/$fileName.jpg")

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        val uploadTask = storageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseUpload", "Failed to upload image: ${exception.message}")
            callback(null)
        }
    }

    private fun saveProfileImageUrls(userId: String, imageUrls: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        userRef.update(mapOf("profileImageUrls" to imageUrls))
            .addOnSuccessListener {
                Log.d("Firestore", "Profile image URLs updated successfully")
                // Navigate to MatchUI after successful upload
                startActivity(Intent(this, MatchUI::class.java))
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating profile image URLs", e)
            }
    }
}
