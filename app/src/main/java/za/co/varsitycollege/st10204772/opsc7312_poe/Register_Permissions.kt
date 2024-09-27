package za.co.varsitycollege.st10204772.opsc7312_poe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.system.exitProcess

class Register_Permissions : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_permissions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Code Begins

        //Continue Button
        var btnpermissions = findViewById<Button>(R.id.btnContinuePermission)
        btnpermissions.setOnClickListener {
            showCustomPermissionDialog()
        }

        //Disallow Button
        var btndisallow = findViewById<Button>(R.id.btndisallow)
        btndisallow.setOnClickListener{
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

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Both permissions granted
                Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                //Next Page
                startActivity( Intent(this, Register_Email::class.java))
            } else {
                // Permissions denied
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}