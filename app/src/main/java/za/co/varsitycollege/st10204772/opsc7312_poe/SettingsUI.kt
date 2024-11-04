@file:Suppress("DEPRECATION")

package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.spotify.sdk.android.auth.LoginActivity
import java.util.Locale
import java.util.concurrent.Executor

class SettingsUI : AppCompatActivity() {

    private lateinit var notificationSwitch: Switch
    private lateinit var biometricSwitch: Switch
    private lateinit var fabLogout: FloatingActionButton
    private lateinit var languageSpinner: Spinner
    private lateinit var btnSaveChange: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var executor: Executor

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Load the locale before setting content view
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("LanguageCode", "en") ?: "en"
        updateLocale(this, languageCode)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings_ui)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        languageSpinner = findViewById(R.id.sp_Language)

        val languages = arrayOf("English", "Xhosa")
        val languageCodes = arrayOf("en", "xh")

        // Populate the spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languageSpinner.adapter = adapter

        // Set the spinner selection based on saved preference
        val currentLanguagePosition = languageCodes.indexOf(languageCode)
        languageSpinner.setSelection(currentLanguagePosition)

        notificationSwitch = findViewById(R.id.switchPush)
        biometricSwitch = findViewById(R.id.switchBio)
        btnSaveChange = findViewById(R.id.btnSaveChange)

        setupBottomNavigation()

        fabLogout = findViewById(R.id.fabLogout)
        fabLogout.setOnClickListener {
            // Get the instance of FirebaseAuth
            val auth = FirebaseAuth.getInstance()

            // Sign out the user
            auth.signOut()

            // Clear user session data from SharedPreferences if necessary
            val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                clear() // or remove specific keys as needed
                apply()
            }

            // Navigate back to the login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the back stack
            startActivity(intent)

            // Optionally, you can show a message indicating successful logout
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        btnSaveChange.setOnClickListener {
            val selectedPosition = languageSpinner.selectedItemPosition
            val newLanguageCode = languageCodes[selectedPosition]
            changeLanguage(newLanguageCode)
        }

        // Initialize switches and their states
        initializeSwitches()
    }

    private fun initializeSwitches() {
        notificationSwitch = findViewById(R.id.switchPush)
        biometricSwitch = findViewById(R.id.switchBio)

        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        val isBiometricEnabled = sharedPreferences.getBoolean("biometric_enabled", true)

        notificationSwitch.isChecked = isNotificationsEnabled
        biometricSwitch.isChecked = isBiometricEnabled

        notificationSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        biometricSwitch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            sharedPreferences.edit().putBoolean("biometric_enabled", isChecked).apply()
            if (isChecked) {
                setupBiometricPrompt()
            }
        }
    }

    private fun changeLanguage(languageCode: String) {
        val newContext = updateLocale(this, languageCode)
        setLanguagePreference(newContext, languageCode)

        // Restart the current activity to apply the new language
        val intent = Intent(newContext, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        newContext.startActivity(intent)
        finish() // Close the current activity
    }

    private fun setLanguagePreference(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("LanguageCode", languageCode).apply()

        // Log the language code for debugging
        Log.d("LanguagePreference", "Language code set: $languageCode")
    }

    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        return context.createConfigurationContext(config)
    }

    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
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
