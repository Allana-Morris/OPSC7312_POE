package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
                    DatabaseReadandWrite().checkLogin(newEmail, newPassword) { isFound ->
                        if (isFound) {
                            DatabaseReadandWrite().loginUser(newEmail, newPassword) { user ->
                                if (user != null) {
                                    Toast.makeText(this, "User Already Exists", Toast.LENGTH_LONG).show()
                                } else {
                                    Log.e(TAG, "Database Read Error")
                                }
                            }
                        } else {
                          User().Email = newEmail
                            User().Password = newPassword
                            User().hasGoogle = false
                            startActivity(Intent(this, Register_About_You::class.java))
                        }
                    }
                } else if (!inpVal.isEmail(newEmail)) {
                    Log.e(TAG, "Invalid Email")
                    Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_LONG).show()
                } else if (!inpVal.isPassword(newPassword)) {
                    Log.e(TAG, "Invalid Password")
                    Toast.makeText(this, "Invalid Password Format", Toast.LENGTH_LONG).show()
                } else {
                    Log.e(TAG, "Error (wtf bro)")
                    Toast.makeText(this, "Input is unable to be Validated", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Log.e(TAG, "Invalid Input")
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_LONG).show()
            }
        }



        }

    }
