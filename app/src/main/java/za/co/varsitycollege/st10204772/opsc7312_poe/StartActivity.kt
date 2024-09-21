package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10204772.opsc7312_poe.ClientID.server_client_id
import za.co.varsitycollege.st10204772.opsc7312_poe.User.GoogleUser


class StartActivity : AppCompatActivity() {

    lateinit var context: Context
    val credentialManager = CredentialManager.create(this)
    val transport = NetHttpTransport()
    val jsonFactory = GsonFactory.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        var btnGoogle = findViewById<Button>(R.id.btnSignUpWithGoogle)

        // Google SSO
        btnGoogle.setOnClickListener{
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(server_client_id)
            .setAutoSelectEnabled(true)
            .setNonce("")
            .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                fetchCredential(request)
            }
        }
    }

    suspend fun fetchCredential(request: GetCredentialRequest){
        coroutineScope {
            launch {
                try{
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@StartActivity

                    )
                } catch (e: GetCredentialException)
                {
                Log.e("CredentialError", "Failed to get credential: ${e.message}")

            } catch (e: Exception) {
            Log.e("UnexpectedError", "An unexpected error occurred: ${e.message}")
        }
            }
        }
    }


    fun handleSignIn(result: GetCredentialResponse){
        val credential = result.credential

        when(credential){

            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
            }

            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                    try{
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val idTokenString = googleIdTokenCredential.idToken

                        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                            .setAudience(listOf(ClientID.google_client_id))
                            .build()
                        val idToken: GoogleIdToken? = verifier.verify(idTokenString)

                        if (idToken != null){
                            val payload: GoogleIdToken.Payload = idToken.payload
                            val gUser = GoogleUser()
                            gUser.setGoogleUser(payload.subject, payload.email, payload.emailVerified, payload["name"] as String?, payload["picture"] as String?, payload["locale"] as String?, payload["family_name"] as String?, payload["given_name"] as String? )
                        }
                        else{
                            Log.e(TAG, "IdToken = Null")
                        }
                    } catch(e: GoogleIdTokenParsingException){
                        Log.e(TAG, "Received invalid google id token response")
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type")
                }
            }

        }
    }


}