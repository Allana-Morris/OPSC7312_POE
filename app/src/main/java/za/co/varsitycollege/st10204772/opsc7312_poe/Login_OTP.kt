package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class Login_OTP : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var intent: Intent = Intent()
    private val inpval = InputValidation()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_login_otp)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //firebase auth with phone num (OTP)
        var edtOTP = findViewById<EditText>(R.id.etxtEmail)
        var btnsubmit = findViewById<Button>(R.id.btncontinue)
        var btnresend = findViewById<TextView>(R.id.txtResend)
        val phone = intent.getStringExtra("cellNum") ?: "0834570603"

        auth = FirebaseAuth.getInstance()


        //Resend OTP
        btnresend.setOnClickListener {

            if (inpval.isString(phone)) {
                if (inpval.isString(phone)) {
                    resendVerificationCode(phone, resendToken)
                }
            }
        }


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(this@Login_OTP, "Invalid request", Toast.LENGTH_LONG).show()
                    }
                    is FirebaseTooManyRequestsException -> {
                        Toast.makeText(this@Login_OTP, "SMS quota exceeded", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                Handler().postDelayed({
                    btnresend.visibility = View.VISIBLE
                }, 60000)
            }
        }

        if (inpval.isString(phone)) {
            Toast.makeText(this, "Phone number retrieved: $phone", Toast.LENGTH_SHORT).show()
            startUp(phone)
        } else {
            Toast.makeText(this, "Phone number is missing", Toast.LENGTH_LONG).show()
        }

        // Submit button functionality
        btnsubmit.setOnClickListener {
            val code = edtOTP.text.toString()
            if (storedVerificationId != null) {
                verifyPhoneNumberWithCode(storedVerificationId!!, code)
            } else {
                Toast.makeText(this, "Please enter a valid OTP", Toast.LENGTH_LONG).show()
            }
        }

    }




    private fun startUp(phoneNumber: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val formattedPhoneNumber = inpval.formatPhoneNumber(phoneNumber)
            Log.d(TAG, "Formatted phone number: $formattedPhoneNumber")
            startPhoneNumberVerification(formattedPhoneNumber)
        } else {
            updateUI(currentUser)
        }
    }



    private fun startPhoneNumberVerification(phoneber: String) {
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneber)  // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
            .setActivity(this)  // Activity (for callback binding)
            .setCallbacks(callbacks)  // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneber: String,
        token: PhoneAuthProvider.ForceResendingToken?,
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneber)  // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
            .setActivity(this)  // (optional) Activity for callback binding
            .setCallbacks(callbacks)  // OnVerificationStateChangedCallbacks
        token?.let { optionsBuilder.setForceResendingToken(it) }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    updateFirestoreUser(user?.phoneNumber ?: "")
                } else {
                    Toast.makeText(this, "Sign-in failed", Toast.LENGTH_LONG).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Invalid code entered
                    }
                }
            }
    }
    // [END sign_in_with_phone]

    private fun updateFirestoreUser(phoneNumber: String) {
        val firestore = FirebaseFirestore.getInstance()
        val usersRef = firestore.collection("Users")

        usersRef.whereEqualTo("phone", phoneNumber).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No user found with this phone number", Toast.LENGTH_LONG).show()
                } else {
                    for (document in documents) {
                        Log.d(TAG, "User found: ${document.id} => ${document.data}")
                        // You can update the current user state in your app based on Firestore data
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user", e)
            }
    }

    private fun updateUI(user: FirebaseUser? = auth.currentUser) {
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
    }
}