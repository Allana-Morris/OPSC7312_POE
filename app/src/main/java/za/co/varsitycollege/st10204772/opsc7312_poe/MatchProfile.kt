package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10204772.opsc7312_poe.databinding.ActivityMatchProfileBinding

class MatchProfile : AppCompatActivity() {

    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var genreRecyclerView: RecyclerView
    private lateinit var MatchUserID: String
    private var adapterSongName: MutableList<String> = mutableListOf()
    private var adapterSongArtist: MutableList<String> = mutableListOf()
    private var adapterArtist: MutableList<String> = mutableListOf()
    private var adapterGenre: MutableList<String> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var songAdapter: SongAdapter
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var genreAdapter: GenreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize data binding
        val binding = ActivityMatchProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView setup with adapters
        artistRecyclerView = binding.RVArtists
        trackRecyclerView = binding.RVSongs
        genreRecyclerView = binding.RVGenre

        artistRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        genreRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapters
        songAdapter = SongAdapter(adapterSongName, adapterSongArtist)
        artistAdapter = ArtistAdapter(adapterArtist)
        genreAdapter = GenreAdapter(adapterGenre)

        // Set adapters to RecyclerViews
        artistRecyclerView.adapter = artistAdapter
        trackRecyclerView.adapter = songAdapter
        genreRecyclerView.adapter = genreAdapter

        // Retrieve MatchUserID from intent
        MatchUserID = intent.getStringExtra("Email").orEmpty()
        if (MatchUserID.isNotEmpty()) {
            GetMatchSpotifyData(MatchUserID)
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        }

        // Bottom navigation setup
        val navbar = binding.BNVNavbarProfileMatch
        navbar.setOnItemSelectedListener { item ->
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
    }

    private fun GetMatchSpotifyData(userEmail: String) {
        db.collection("Users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val dbSongName = document.get("topSongs") as? List<String> ?: emptyList()
                    val dbSongArtist = document.get("songArtist") as? List<String> ?: emptyList()
                    val dbArtists = document.get("topArtists") as? List<String> ?: emptyList()
                    val dbGenre = document.get("topGenres") as? List<String> ?: emptyList()

                    // Update adapter data
                    adapterSongName.addAll(dbSongName)
                    adapterSongArtist.addAll(dbSongArtist)
                    adapterArtist.addAll(dbArtists)
                    adapterGenre.addAll(dbGenre)

                    // Notify adapters of data change
                    songAdapter.notifyDataSetChanged()
                    artistAdapter.notifyDataSetChanged()
                    genreAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
