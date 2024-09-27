package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Register_Image_Upload : AppCompatActivity() {

    private var imageList: MutableList<Bitmap> = mutableListOf()
    private var currentImageViewIndex: Int = -1


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
            if (imageList.any { false }) {
                Toast.makeText(this, "Please select six images", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // All images are selected, go to the next page
                User().ProfilePhotos = imageList
                startActivity(Intent(this, Register_Spotify_Link::class.java))
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
}


