package za.co.varsitycollege.st10204772.opsc7312_poe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.Executor
import kotlin.system.exitProcess

class Register_Permissions : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 101
    private val BIOMETRIC_PERMISSION_CODE = 102
    private val NOTIFICATION_PERMISSION_CODE = 103

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_permissions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Continue Button
        val btnpermissions = findViewById<Button>(R.id.btnContinuePermission)
        btnpermissions.setOnClickListener {
            requestBiometricPermissions()
            requestNotificationPermissions()
            requestLocationPermissions()
        }

        // Disallow Button
        val btndisallow = findViewById<Button>(R.id.btndisallow)
        btndisallow.setOnClickListener {
            exitProcess(-1)
        }

        // Set up biometric prompt
        setupBiometricPrompt()
    }

    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun requestLocationPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show()
            navigateToNextActivity()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_CODE)
        }
    }

    private fun requestNotificationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            showNotificationPrompt()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
        }
    }

    private fun requestBiometricPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED) {
            showBiometricPrompt()
        } else {
            // Request biometric permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.USE_BIOMETRIC), BIOMETRIC_PERMISSION_CODE)
        }
    }

    private fun showBiometricPrompt() {
        val message = "Do you want to enable biometric sign-in?"
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        AlertDialog.Builder(this)
            .setTitle("Biometric Sign-In")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                sharedPreferences.edit().putBoolean("biometric_enabled", true).apply()
                Toast.makeText(this, "Biometric sign-in enabled", Toast.LENGTH_SHORT).show()

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                sharedPreferences.edit().putBoolean("biometric_enabled", false).apply()
                Toast.makeText(this, "Biometric sign-in disabled", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showNotificationPrompt() {
        val message = "Do you want to enable notifications?"
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        AlertDialog.Builder(this)
            .setTitle("Notifications")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                sharedPreferences.edit().putBoolean("notifications_enabled", true).apply()
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                sharedPreferences.edit().putBoolean("notifications_enabled", false).apply()
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
                navigateToNextActivity()
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                    navigateToNextActivity()
                } else {
                    Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToNextActivity() {
        Log.d("Navigation", "Navigating to Register_Email activity")
        val intent = Intent(this, Register_Email::class.java)
        startActivity(intent)
        finish()
    }
}
