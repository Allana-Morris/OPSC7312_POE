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

        var txtEmail = findViewById<EditText>(R.id.etxtEmailReg)
        var txtPassword = findViewById<EditText>(R.id.etxtPasswordReg)
        var btnnext = findViewById<Button>(R.id.btnContinueReg)
        var inpval = InputValidation()

        btnnext.setOnClickListener {
           var newemail = txtEmail.text
            var newpassword = txtPassword.text
            if ((inpval.isStringInput(newemail)) && (inpval.isStringInput(newpassword))) {
                var email = newemail.toString()
                var password = newpassword.toString()

                if ((inpval.isEmail(email)) && (inpval.isPassword(password))) {
                    DatabaseReadandWrite().checkLogin(email, password) { isFound ->

                        if (isFound) {
                            DatabaseReadandWrite().loginUser(email, password) { user ->
                                if (user != null) {
                                    Toast.makeText(this, "User Already Exists", Toast.LENGTH_LONG).show()
                                } else {
                                    Log.e(TAG, "Database Read Error")
                                    return@loginUser
                                }
                            }
                        } else {
                          User().Email = email
                            User().Password = password
                            User().hasGoogle = false
                            startActivity(Intent(this, Register_About_You::class.java))
                        }
                    }
                } else if (!inpval.isEmail(email)) {
                    Log.e(TAG, "Invalid Email")
                    Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_LONG).show()
                } else if (!inpval.isPassword(password)) {
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
