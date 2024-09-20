package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class Register_Spotify_Link : AppCompatActivity() {

    val REQUEST_CODE = 1337
    val REDIRECT_URI = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_spotify_link)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Spotify Authentication
        val btnSpotify = findViewById<Button>(R.id.btnspotifysearch)

        btnSpotify.setOnClickListener{
            val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            builder.setScopes(arrayOf("streaming"))
            builder.setShowDialog(true)
            val request = builder.build()
            AuthorizationClient.openLoginInBrowser(this, request)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri: Uri? = intent.data
        uri?.let{
            val response = AuthorizationResponse.fromUri(uri)

            when(response.type){
                AuthorizationResponse.Type.TOKEN -> {
                    //Yippee
                }
                AuthorizationResponse.Type.ERROR -> {
                    //Nooooooo T_T
                }

                else -> {
                    //Other
                }
            }
        }
    }


}