package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class Register_Email : AppCompatActivity() {
    var user: User = User()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtEmail = findViewById<EditText>(R.id.etxtEmailReg)
        val txtPassword = findViewById<EditText>(R.id.etxtPasswordReg)
        val btnNext = findViewById<Button>(R.id.btnContinueReg)
        val inpVal = InputValidation()

        btnNext.setOnClickListener {
            val newEmail = txtEmail.text.toString().trim() // trim spaces
            val newPassword = txtPassword.text.toString().trim()

            // Check if the input is valid (non-empty strings)
            if (inpVal.isStringInput(newEmail) && inpVal.isStringInput(newPassword)) {
                // Validate the format of email and password
                if (inpVal.isEmail(newEmail) && inpVal.isPassword(newPassword)) {
                    // Set user details for registration
                    user.Email = newEmail
                    user.Password = newPassword
                    user.hasGoogle = false

                    // Register and save the new user to Firestore
                    DatabaseReadandWrite().registerUser(user) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show()
                            var loguser = User()
                            loguser.Email = newEmail
                            loggedUser.user = loguser
                            retrieveFcmToken(newEmail)
                            startActivity(Intent(this, Register_About_You::class.java))
                        } else {
                            if (errorMessage?.contains("already in use") == true) {
                                // Firebase will throw an error if the email is already registered
                                Toast.makeText(this, "User Already Exists", Toast.LENGTH_LONG).show()
                            } else {
                                Log.e(TAG, "Failed to register user: $errorMessage")
                                Toast.makeText(this, "Failed to save user: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    // Handle invalid email or password format
                    if (!inpVal.isEmail(newEmail)) {
                        Log.e(TAG, "Invalid Email")
                        Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_LONG).show()
                    }
                    if (!inpVal.isPassword(newPassword)) {
                        Log.e(TAG, "Invalid Password")
                        Toast.makeText(this, "Password must be at least 6 characters and meet other criteria", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.e(TAG, "Invalid Input: Empty fields")
                Toast.makeText(this, "Please provide both email and password", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("AccountManager", "FCM Token: $fcmToken")

                // Save token locally in SharedPreferences for quick access if needed
                saveTokenToPreferences(userId ,fcmToken, this)
                updateFcmToken(fcmToken)

            } else {
                Log.w("AccountManager", "Failed to get FCM token", task.exception)
            }
        }
    }

    private fun saveTokenToPreferences(userid: String, token: String?, context: Context) {

        val sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("${userid}_fcmToken", token).apply()
    }

    fun updateFcmToken(fcmToken: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Get the logged user's email
        val email = loggedUser.user?.Email

        if (email != null) {
            // Reference to the Users collection
            val usersCollection = firestore.collection("Users")

            // Query to find the user document where email matches
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]

                        // Update the document with the FCM token
                        userDocument.reference.update("fcmToken", fcmToken)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "FCM token updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating FCM token: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
        }
    }


}

