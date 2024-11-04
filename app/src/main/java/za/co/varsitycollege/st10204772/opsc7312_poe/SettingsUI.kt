@file:Suppress("DEPRECATION")

package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.biometric.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.spotify.sdk.android.auth.LoginActivity
import java.util.concurrent.Executor
import kotlin.system.exitProcess

class SettingsUI : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private lateinit var biometricSwitch: Switch
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_ui)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        notificationSwitch = findViewById(R.id.switchPush)
        biometricSwitch = findViewById(R.id.switchBio)
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        setupBottomNavigation()

        var fabLogout = findViewById<FloatingActionButton>(R.id.fabLogout)

        fabLogout.setOnClickListener {
            // Get the instance of FirebaseAuth
            val auth = FirebaseAuth.getInstance()
            loggedUser.user = User()

            // Sign out the user
            auth.signOut()

            // Clear user session data from SharedPreferences if necessary
            val SharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with(SharedPreferences.edit()) {
                clear() // or remove specific keys as needed
                apply()
            }

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
            startActivity(intent)
        }

        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val isBiometricEnabled = sharedPreferences.getBoolean("biometric_enabled", true)


        notificationSwitch.isChecked = isNotificationsEnabled
        biometricSwitch.isChecked = isBiometricEnabled

        // Set listener for the notification switch
        notificationSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            // Save notification preference
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
            // Handle enabling/disabling notifications here
            if (isChecked) {
                // Enable notifications
            } else {
                // Disable notifications
            }
        }

        // Set listener for the biometric switch
        biometricSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            // Save biometric preference
            sharedPreferences.edit().putBoolean("biometric_enabled", isChecked).apply()
            // Handle enabling/disabling biometric sign-in
            if (isChecked) {
                setupBiometricPrompt()
            } else {

            }
        }
    }

    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Handle successful fingerprint authentication
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!",
                        Toast.LENGTH_SHORT
                    ).show()
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle error in authentication
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Handle failed authentication
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Sign-In")
            .setSubtitle("Use your biometric credential to sign in")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }


    private fun setupBottomNavigation() {
        val navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Profile)
        navbar.selectedItemId = -1
        navbar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_match -> startActivity(Intent(this, MatchUI::class.java))
                R.id.nav_like -> startActivity(Intent(this, Liked_you::class.java))
                R.id.nav_chat -> startActivity(Intent(this, Contact::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileUI::class.java))
                else -> false
            }
            true
        }
    }
}