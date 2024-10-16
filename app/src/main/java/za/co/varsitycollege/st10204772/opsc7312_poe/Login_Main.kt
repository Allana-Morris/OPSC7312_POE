package za.co.varsitycollege.st10204772.opsc7312_poe

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.CLIENT_ID
import java.io.IOException
import java.util.concurrent.Executor

class Login_Main : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private var mOkHttpClient = OkHttpClient.Builder().build()
    private lateinit var sAccessToken: String

    //Fingerprint
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var btnLogin = findViewById<Button>(R.id.btnContinueLogin)
        var userEmail = findViewById<EditText>(R.id.etxtEmailLogin)
        var userPassword = findViewById<EditText>(R.id.etxtPassword)
        var signup = findViewById<TextView>(R.id.txtSignUpRedirectLogin)
        var uEmail = userEmail.text
        var uPass = userPassword.text
        var inpval = InputValidation()

        signup.setOnClickListener {
            var intent = Intent(this, Register_Permissions::class.java)
            startActivity(intent)
        }

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Setup biometric prompt
        setupBiometricPrompt()

        // Always show fingerprint prompt when the login screen opens
        showBiometricPrompt()

        btnLogin.setOnClickListener {
            if ((inpval.isStringInput(uEmail.toString())) && (inpval.isStringInput(uPass.toString()))) {
                var email = uEmail.toString()
                var password = uPass.toString()

                if ((inpval.isEmail(email)) && (inpval.isPassword(password))) {
                    DatabaseReadandWrite().checkLogin(email, password) { isFound ->

                        if (isFound) {
                            DatabaseReadandWrite().loginUser(email, password) { user ->
                                if (user != null) {
                                    authenticateWithSpotify()

                                    // Directly navigate to the profile activity
                                    navigateToProfile()
                                } else {
                                    Log.e(TAG, "Failed to load user")
                                }
                            }
                        } else {
                            Log.e(TAG, "User Not Found or Failed to load user")
                            Toast.makeText(this, "User Not Found", Toast.LENGTH_LONG).show()
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

    private fun authenticateWithSpotify() {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            ClientID.REDIRECT_URI2 // Ensure this matches your registered redirect URI
        )
        builder.setScopes(
            arrayOf(
                "user-read-private",
                "user-read-email",
                "user-top-read"
            )
        ) // Add scopes as needed
        val request = builder.build()

        AuthorizationClient.openLoginInBrowser(this, request)
    }

    // Function to set up BiometricPrompt
    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Handle successful fingerprint authentication
                Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                // Directly navigate to the profile activity
                navigateToProfile()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Handle error in authentication
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Handle failed authentication
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Login")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()
    }

    // Show fingerprint prompt
    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }

    // Navigate to Profile Activity on successful authentication
    private fun navigateToProfile() {
        val intent = Intent(this, ProfileUI::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleAuthorizationResponse(intent)
        }
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        val response = AuthorizationClient.getResponse(1001, intent)
        when (response.type) {
            AuthorizationResponse.Type.TOKEN -> {
                // Handle successful response
                sAccessToken = response.accessToken
                loggedUser.user?.Name = sAccessToken
                CallSpotifyFun()
            }

            AuthorizationResponse.Type.ERROR -> {
                // Handle error response
                Log.e(this@Login_Main.toString(), "Access Token Issue")
            }

            else -> {
                // Handle other cases
                Log.e(this@Login_Main.toString(), "Access Token Issue")
            }
        }
    }


    private fun CallSpotifyFun() {
        fetchTopGenre()
        fetchTopSongs()
        fetchTopArtists()
    }

    private fun fetchTopGenre() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=50") // Fetch top artists
            .addHeader("Authorization", "Bearer $sAccessToken") // Use access token
            .build()

        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = response.body?.string()
                        return
                    }

                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val artists = jsonResponse.getJSONArray("items")
                    val genreMap = mutableMapOf<String, Int>() // To count genres

                    // Count occurrences of each genre
                    for (i in 0 until artists.length()) {
                        val artist = artists.getJSONObject(i)
                        val genres = artist.getJSONArray("genres")
                        for (j in 0 until genres.length()) {
                            val genre = genres.getString(j)
                            genreMap[genre] = genreMap.getOrDefault(genre, 0) + 1
                        }
                    }

                    // Sort genres by count and retrieve the top 3
                    val topGenres = genreMap.toList()
                        .sortedByDescending { it.second }
                        .take(3)
                        .map { it.first } // Get only the genre names

                    // Store the top genres in Firestore
                    storeTopGenresInFirestore(topGenres)
                }
            }
        })
    }

    // Store top genres in Firestore
    private fun storeTopGenresInFirestore(topGenres: List<String>) {
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

                        // Update the document with the top genres
                        userDocument.reference.update("topGenres", topGenres)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top genres updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top genres: ${e.message}",
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

    private fun fetchTopSongs() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/tracks?limit=3") // Fetch top tracks
            .addHeader("Authorization", "Bearer $sAccessToken") // Use access token
            .build()

        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = response.body?.string()
                        return
                    }

                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val songs = jsonResponse.getJSONArray("items")
                    val topSongs = mutableListOf<String>() // To hold top song names
                    val SongArtistName = mutableListOf<String>()
                    val albumArt = mutableListOf<String>()

                    // Retrieve the top 3 song names
                    for (i in 0 until songs.length()) {
                        val song = songs.getJSONObject(i)
                        val songName = song.getString("name")

                        val artistArray = song.getJSONArray("artists")
                        val artistName = artistArray.getJSONObject(0).getString("name")

                        // Get the album artwork URL
                        val album = song.getJSONObject("album")
                        val images = album.getJSONArray("images")
                        val artworkUrl = images.getJSONObject(0)
                            .getString("url") // Usually, index 0 is the highest resolution

                        topSongs.add(songName)
                        SongArtistName.add(artistName)
                        albumArt.add(artworkUrl)

                    }


                    // Store the top songs in Firestore
                    storeTopSongsInFirestore(topSongs, SongArtistName, albumArt)
                }
            }
        })
    }

    // Store top songs in Firestore
    private fun storeTopSongsInFirestore(
        topSongs: List<String>,
        songartist: List<String>,
        albumart: List<String>
    ) {
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

                        // Update the document with the top songs
                        userDocument.reference.update("topSongs", topSongs)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top songs updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top songs: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        userDocument.reference.update("songArtist", songartist)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top songs artists updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top songs artists: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        userDocument.reference.update("albumArt", albumart)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top songs updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top songs: ${e.message}",
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

    private fun fetchTopArtists() {
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=3") // Fetch top artists
            .addHeader("Authorization", "Bearer $sAccessToken") // Use access token
            .build()

        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                runOnUiThread {
                    Toast.makeText(
                        this@Login_Main,
                        "Error fetching top artists: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = response.body?.string()
                        runOnUiThread {
                            Toast.makeText(
                                this@Login_Main,
                                "Failed to fetch top artists: $errorBody",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return
                    }

                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    val artists = jsonResponse.getJSONArray("items")

                    val topArtists = mutableListOf<String>() // Store top artist names
                    for (i in 0 until artists.length()) {
                        val artist = artists.getJSONObject(i)
                        val artistName = artist.getString("name") // Get artist name
                        topArtists.add(artistName) // Add to list
                    }
                    topArtists.add("Peanits")
                    // Store top songs and artists in Firestore
                    storeTopArtistsInFirestore(topArtists)

                }
            }
        })
    }

    private fun storeTopArtistsInFirestore(topArtists: List<String>) {
        val firestore = FirebaseFirestore.getInstance()
        val email = loggedUser.user?.Email

        if (email != null) {
            // Reference to the Users collection
            val usersCollection = firestore.collection("Users")

            // Query to find the user document where email matches
            usersCollection.whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]

                        // Update the document with the top artists
                        userDocument.reference.update("topArtists", topArtists)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Top artists updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating top artists: ${e.message}",
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

    override fun onDestroy() {
        val mCall: Call? = null
        mCall?.cancel() // Cancel any ongoing API requests
        super.onDestroy()
    }

    private fun saveTokens(accessToken: String) {
        // Implement secure storage for tokens
        loggedUser.user?.Name = accessToken

    }
}

