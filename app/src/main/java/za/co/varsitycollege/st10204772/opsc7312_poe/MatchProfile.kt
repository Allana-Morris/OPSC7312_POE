package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = ActivityMatchProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artistRecyclerView = findViewById(R.id.RV_Artists)
        trackRecyclerView = findViewById(R.id.RV_Songs)

        artistRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        var intent = Intent.getIntent("")
        var sAccessToken = intent.getStringExtra("AccessToken")
        sAccessToken?.let { token ->
            fetchUserTopItems(token)
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
            .url("https://api.spotify.com/v1/me/top/artists?limit=50") // More items to determine genres
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

                    // Sort genres by count and get top 3
                    genreCount.entries.sortedByDescending { it.value }.take(3).forEach { entry ->
                        genres.add(Genre(entry.key))
                    }

                    runOnUiThread {
                        genreRecyclerView.adapter = GenreAdapter(genres) // Implement this adapter
                    }
                }
            }
        })
    }