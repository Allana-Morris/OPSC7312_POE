package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class Register_Spotify_Link : AppCompatActivity() {

    val REQUEST_CODE = 1337

    val REDIRECT_URI: String = "spotify-sdk://auth"

    val AUTH_TOKEN_REQUEST_CODE: Int = 0x10

    val AUTH_CODE_REQUEST_CODE: Int = 0x11

    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mAccessToken: String? = null
    private var mAccessCode: String? = null
    private var mCall: Call? = null

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
        val btnsubmit = findViewById<Button>(R.id.btncontinue)

        btnSpotify.setOnClickListener{
            val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
            builder.setScopes(arrayOf("streaming"))
            builder.setShowDialog(true)
            val request = builder.build()
            AuthorizationClient.openLoginInBrowser(this, request)
        }

        btnsubmit.setOnClickListener {

        }
    }
    override fun onDestroy() {
        cancelCall()
        super.onDestroy()
    }

    fun onGetUserProfileClicked(view: View) {
        if (mAccessToken == null) {
            val snackbar = Snackbar.make(
                findViewById(R.id.main), R.string.warning_need_token, Snackbar.LENGTH_SHORT
            )
            snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.spotify_green))
            snackbar.show()
            return
        }

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .addHeader("Authorization", "Bearer $mAccessToken")
            .build()

        cancelCall()
        mCall = mOkHttpClient.newCall(request)

        mCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                setResponse("Failed to fetch data: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    setResponse(jsonObject.toString(3))
                } catch (e: JSONException) {
                    setResponse("Failed to parse data: $e")
                }
            }
        })
    }
    fun onRequestCodeClicked(view: View) {
        val request = getAuthenticationRequest(AuthorizationResponse.Type.CODE)
        AuthorizationClient.openLoginActivity(this, AUTH_CODE_REQUEST_CODE, request)
    }

    fun onRequestTokenClicked(view: View) {
        val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
    }

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest {
        return AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
            .setShowDialog(false)
            .setScopes(arrayOf("user-read-email"))
            .setCampaign("your-campaign-token")
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = AuthorizationClient.getResponse(resultCode, data)

        when (requestCode) {
            AUTH_TOKEN_REQUEST_CODE -> {
                mAccessToken = response.accessToken
                updateTokenView()
            }
            AUTH_CODE_REQUEST_CODE -> {
                mAccessCode = response.code
                updateCodeView()
            }
        }
    }

  private fun setResponse(text: String) {
        runOnUiThread {
           // val responseView = findViewById<TextView>(R.id.response_text_view)
           // responseView.text = text
        }
    }

    private fun updateTokenView() {
       // val tokenView = findViewById<TextView>(R.id.token_text_view)
       // tokenView.text = getString(R.string.token, accessToken)
    }

    private fun updateCodeView() {
       // val codeView = findViewById<TextView>(R.id.code_text_view)
       // codeView.text = getString(R.string.code, accessCode)
    }

    private fun cancelCall() {
        mCall?.cancel()
    }

    private fun getRedirectUri(): Uri {
        return Uri.parse(REDIRECT_URI)
    }

}