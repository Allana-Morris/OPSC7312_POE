package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login_Main : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogin = findViewById<Button>(R.id.btnContinueLogin)
        val userEmail = findViewById<EditText>(R.id.etxtEmailLogin)
        val userPassword = findViewById<EditText>(R.id.etxtPassword)
        val inpval = InputValidation()

        btnLogin.setOnClickListener {
            val email = userEmail.text.toString().trim()  // Trim input
            val password = userPassword.text.toString().trim()

            if (inpval.isStringInput(email) && inpval.isStringInput(password)) {
                if (inpval.isEmail(email) && inpval.isPassword(password)) {
                    // Attempt login using Firebase Auth
                    DatabaseReadandWrite().loginUser(email, password) { user ->
                        if (user != null) {
                            val userId = user.uid  // Get the logged-in user's UID
                            // Check if the user profile exists and is complete
                            DatabaseReadandWrite().checkUserProfileExists(userId) { isProfileComplete ->
                                if (isProfileComplete) {
                                    // Redirect to main app if the profile is complete
                                    val intent = Intent(this, MatchUI::class.java)
                                    startActivity(intent)
                                } else {
                                    // Redirect to registration if the profile is incomplete
                                    val intent = Intent(this, Register_About_You::class.java)
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Log.e(TAG, "User Not Found or Failed to load user")
                            Toast.makeText(this, "User Not Found", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    if (!inpval.isEmail(email)) {
                        Log.e(TAG, "Invalid Email")
                        Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_LONG).show()
                    }
                    if (!inpval.isPassword(password)) {
                        Log.e(TAG, "Invalid Password")
                        Toast.makeText(this, "Invalid Password Format", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.e(TAG, "Invalid Input")
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_LONG).show()
            }
        }
    }
}

