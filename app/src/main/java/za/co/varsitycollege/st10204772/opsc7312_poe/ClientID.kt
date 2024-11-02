package za.co.varsitycollege.st10204772.opsc7312_poe

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object ClientID {
    const val CLIENT_ID = "eb9b8af983d94603adaa1d212cf58980"
    const val REDIRECT_URI = "myapp://callback"
    const val REDIRECT_URI2 = "myapp://callback2"
    const val AUTH_URL = "https://accounts.spotify.com/authorize"
    const val TOKEN_URL = "https://accounts.spotify/com/api/token"
    const val app_client_id =
        "905988466931-7dgl1beg0omvj4k1r0ct11sos1eghgsv.apps.googleusercontent.com"
    const val server_client_id =
        "905988466931-udtk2slaq94ti7dspdvferlbca4n6o0m.apps.googleusercontent.com"

    fun generateCodeVerifierAndChallenge(): Pair<String, String> {
        val codeVerifier = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(ByteArray(32).apply { SecureRandom().nextBytes(this) })
        val codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.UTF_8))
        )

        return Pair(codeVerifier, codeChallenge)
    }

    data class TokenResponse(
        val access_token: String,
        val token_type: String,
        val expires_in: Int,
        val refresh_token: String? // Optional
    )

}

