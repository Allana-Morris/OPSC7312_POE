package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import za.co.varsitycollege.st10204772.opsc7312_poe.databinding.ActivityMatchProfileBinding
import java.io.IOException

class MatchProfile : AppCompatActivity() {

    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var genreRecyclerView: RecyclerView
    private lateinit var MatchUserID: String
    private  var adapterSongName: MutableList<String> = mutableListOf()
    private var adapterSongArtist: MutableList<String> = mutableListOf()
    private var adapterArtist: MutableList<String> = mutableListOf()
    private var adapterGenre: MutableList<String> = mutableListOf()
    private val db = FirebaseFirestore.getInstance()
    private var intent= Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = ActivityMatchProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artistRecyclerView = findViewById(R.id.RV_Artists)
        trackRecyclerView = findViewById(R.id.RV_Songs)
        genreRecyclerView = findViewById(R.id.RV_Genre)

        artistRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        genreRecyclerView.layoutManager = LinearLayoutManager(this)

        intent =getIntent()
        MatchUserID = intent.getStringExtra("Email").toString()

        GetMatchSpotifyData(MatchUserID)

        SongAdapter(adapterSongName, adapterSongArtist)
        ArtistAdapter(adapterArtist)
        GenreAdapter(adapterGenre)

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
    }

    private fun GetMatchSpotifyData(userEmail: String){
        db.collection("Users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener{ documents ->
                for (document in documents){
                    val dbSongName = document.get("topSongs") as? MutableList<String> ?: emptyList()
                    val dbSongArtist = document.get("songArtist") as? MutableList<String>?: emptyList()
                    val dbArtists = document.get("topArtists") as? MutableList<String>?: emptyList()
                    val dbGenre = document.get("topGenres") as? MutableList<String>?: emptyList()

                    adapterSongName.addAll(dbSongName)
                    adapterSongArtist.addAll(dbSongArtist)
                    adapterArtist.addAll(dbArtists)
                    adapterGenre.addAll(dbGenre)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }
}


