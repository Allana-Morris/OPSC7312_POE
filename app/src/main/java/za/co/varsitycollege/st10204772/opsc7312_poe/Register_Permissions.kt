package za.co.varsitycollege.st10204772.opsc7312_poe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
// import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.system.exitProcess

class Register_Permissions : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 101
    private val BIOMETRIC_PERMISSION_CODE = 102

    // Required for Biometric Prompt
    // private lateinit var biometricPrompt: BiometricPrompt
    // private lateinit var promptInfo: BiometricPrompt.PromptInfo
    // private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_permissions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Code Begins

        // Setup biometric prompt here
        // setupBiometricPrompt()

        // Continue Button
        val btnpermissions = findViewById<Button>(R.id.btnContinuePermission)
        btnpermissions.setOnClickListener {
            showCustomPermissionDialog()
        }

        // Disallow Button
        val btndisallow = findViewById<Button>(R.id.btndisallow)
        btndisallow.setOnClickListener {
            exitProcess(-1)
        }
    }

    private fun showCustomPermissionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app requires access to your location to show relevant information based on your location.")
            .setPositiveButton("Allow") { _, _ ->
                // When the user clicks 'Allow', request the permissions
                requestLocationPermissions()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Permission denied by user.", Toast.LENGTH_SHORT).show()
            }
            .create()

        dialog.show()
    }

    private fun requestLocationPermissions() {
        // Check if permissions are already granted
        if ((ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            // Permissions are already granted
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show()

            // Navigate to the next page directly as permissions are granted
            navigateToNextActivity()
        } else {
            // Request both COARSE and FINE location permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Location permissions granted
                    Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                    // Navigate to the next page after permissions are granted
                    navigateToNextActivity()
                } else {
                    // Location permissions denied
                    Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
                }
            }

            /*
            BIOMETRIC_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Biometric permission granted
                    Toast.makeText(this, "Biometric permission granted", Toast.LENGTH_SHORT).show()
                    showBiometricPrompt()  // Show biometric prompt after permission is granted
                } else {
                    // Biometric permission denied
                    Toast.makeText(this, "Biometric permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            */
        }
    }

    // Navigate to the next activity (Register_Email)
    private fun navigateToNextActivity() {
        Log.d("Navigation", "Navigating to Register_Email activity")
        val intent = Intent(this, Register_Email::class.java)
        startActivity(intent)
        finish()  // End current activity so the user cannot come back
    }
}
