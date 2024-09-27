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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class Register_Image_Upload : AppCompatActivity() {

    private var imageList: MutableList<Bitmap> = mutableListOf()
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

        val btncontinue = findViewById<Button>(R.id.btnContinueImage)
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
                    // Convert the selected Uri to a Bitmap and store it in the list
                    selectedImageUri?.let { uri ->
                        val bitmap = uriToBitmap(uri)
                        if (currentImageViewIndex != -1) {
                            // Update the clicked ImageView with the selected image
                            imageViews[currentImageViewIndex].setImageBitmap(bitmap)
                            // Store the image in the list
                            if (bitmap != null) {
                                imageList[currentImageViewIndex] = bitmap
                            }
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

        btncontinue.setOnClickListener {
            if (imageList.any { it == null }) {
                Toast.makeText(this, "Please select six images", Toast.LENGTH_SHORT).show()
            } else {
                val imageUrls = mutableListOf<String>()
                imageList.forEachIndexed { index, bitmap ->
                    uploadImageToFirebaseStorage(bitmap, "image_$index") { downloadUrl ->
                        if (downloadUrl != null) {
                            imageUrls.add(downloadUrl)
                            if (imageUrls.size == imageList.size) {
                                saveProfileImageUrls("user_id", imageUrls)
                                startActivity(Intent(this, Register_Spotify_Link::class.java))
                            }
                        } else {
                            Toast.makeText(this, "Failed to upload image $index", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // For Android P (API 28) and above
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                // For older Android versions
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, fileName: String, callback: (String?) -> Unit) {
        // Create a reference to the image location in Firebase Storage
        val storageRef = storage.child("profile_images/$fileName.jpg")

        // Convert Bitmap to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageData = byteArrayOutputStream.toByteArray()

        // Upload the ByteArray to Firebase Storage
        val uploadTask = storageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            // Get the download URL after successful upload
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                callback(downloadUrl) // Return the download URL to the caller
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseUpload", "Failed to upload image: ${exception.message}")
            callback(null)
        }
    }

    private fun saveProfileImageUrls(userId: String, imageUrls: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        val userProfileData = mapOf(
            "profileImageUrls" to imageUrls
        )

        userRef.update(userProfileData)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile image URLs updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating profile image URLs", e)
            }
    }

}


