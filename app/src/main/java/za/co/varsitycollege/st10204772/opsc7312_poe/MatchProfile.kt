package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import za.co.varsitycollege.st10204772.opsc7312_poe.databinding.ActivityMatchProfileBinding
import java.io.IOException

class MatchProfile : AppCompatActivity() {

    private val mOkHttpClient: OkHttpClient = OkHttpClient()
    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var genreRecyclerView: RecyclerView
    private val genres = mutableListOf<String>()

    private val sStorage = SecureStorage(this)
    private var spotifyAccessToken: String? = sStorage.getID("ACCESS_TOKEN")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = ActivityMatchProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artistRecyclerView = findViewById(R.id.RV_Artists)
        trackRecyclerView = findViewById(R.id.RV_Songs)

        artistRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.layoutManager = LinearLayoutManager(this)



        var navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_ProfileMatch)

        navbar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_match -> {
                    startActivity(Intent(this, MatchUI::class.java))
                    true
                }
                R.id.nav_like -> {
                    startActivity(Intent(this, Liked_you::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, Contact::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileUI::class.java))
                    true
                }
                else -> false
            }
        }


        if (spotifyAccessToken != null) {
            fetchUserTopItems(spotifyAccessToken!!)
        } else {
            // Handle the case where the access token is not available (e.g., show login)
        }
    }

    private fun fetchUserTopItems(accessToken: String) {
        // Fetch Top Artists
        val artistsRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=3") // Limit to top 3
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        mOkHttpClient.newCall(artistsRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val items = jsonObject.getJSONArray("items")

                    for (i in 0 until items.length()) {
                        val artist = items.getJSONObject(i)
                        SpotifyData.Artists().artistID.add(artist.getString("id"))
                        SpotifyData.Artists().artistName.add(artist.getString("name"))
                    }

                    runOnUiThread {
                        // Update RecyclerView with artist data
                        artistRecyclerView.adapter = ArtistAdapter(SpotifyData.Artists()) // Implement this adapter
                    }

                    // Fetch Top Genres (aggregate genres from top artists)
                    fetchTopGenres(accessToken)
                }
            }
        })

        // Fetch Top Tracks
        val tracksRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/tracks?limit=3") // Limit to top 3
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        mOkHttpClient.newCall(tracksRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val items = jsonObject.getJSONArray("items")

                    for (i in 0 until items.length()) {
                        val track = items.getJSONObject(i)
                        SpotifyData.Songs().songID.add(track.getString("id"))
                        SpotifyData.Songs().songName.add(track.getString("name"))
                        SpotifyData.Songs().albumName.add(track.getJSONObject("album").getString("name"))
                        SpotifyData.Songs().artistID.add(track.getJSONArray("artists").getJSONObject(0).getString("id")) // Assuming one artist

                        val albumImageUrl = track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")
                        SpotifyData.Songs().albumpicUrl.add(Uri.parse(albumImageUrl))
                    }

                    runOnUiThread {
                        // Update RecyclerView with song data
                        trackRecyclerView.adapter = SongAdapter(SpotifyData.Songs()) // Implement this adapter
                    }
                }
            }
        })
    }

    private fun fetchTopGenres(accessToken: String) {
        val genresRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me/top/artists?limit=50")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        mOkHttpClient.newCall(genresRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Handle failure
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body?.string() ?: "")
                    val items = jsonObject.getJSONArray("items")
                    val genreCount = mutableMapOf<String, Int>()

                    for (i in 0 until items.length()) {
                        val artist = items.getJSONObject(i)
                        val genresArray = artist.getJSONArray("genres")
                        for (j in 0 until genresArray.length()) {
                            val genreName = genresArray.getString(j)
                            genreCount[genreName] = genreCount.getOrDefault(genreName, 0) + 1
                        }
                    }

                    // Get the top 3 unique genres
                    val topGenres = genreCount.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .map { it.key }

                    genres.addAll(topGenres)

                    runOnUiThread {
                        genreRecyclerView.adapter = GenreAdapter(genres) // Implement this adapter
                    }
                }
            }
        })
    }
    }