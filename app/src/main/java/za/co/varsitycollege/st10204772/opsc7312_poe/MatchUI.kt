package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class MatchUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    private var UserSeen: Boolean = false

    var count = 0;

    // Reference to Firestore
    val firestore = FirebaseFirestore.getInstance()
    val usersCollection = firestore.collection("Users")

    // Get the logged-in user's email
    val loggedUserEmail = loggedUser.user?.Email

    //  @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_ui)

        getUsers()
    }

    fun CheckUserUnseen(matchemail: String?) {
        UserSeen = false  // Reset for each check
        for (seenuser in loggedUser.shownList) {
            if (seenuser == matchemail) {
                UserSeen = true
                break
            }
        }
    }


    private fun getUsers() {
        usersCollection.whereNotEqualTo("albumArt", null).limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var user = MatchUser();
                val matchUsers = mutableListOf<MatchUser>() // List to hold matched users

                for (document in querySnapshot.documents) {
                    // Filter out documents where email matches the logged-in user
                    val userEmail = document.getString("email")
                    if (userEmail != loggedUserEmail) {
                        CheckUserUnseen(userEmail)
                        if (!UserSeen) {
                            val matchUser = MatchUser().apply {
                                Name = document.getString("name") ?: ""
                                Age = document.getLong("age")?.toInt() ?: 0
                                Email = userEmail ?: ""
                                Gender = document.getString("gender") ?: ""
                                Pronoun = document.getString("pronoun") ?: ""
                                profilePictureUrl = document.getList("profileImageUrls")
                                topGenre = document.getList("topGenres")?.map { it.toString() }
                                    ?: emptyList()
                                topArtist =
                                    document.getList("topArtists")?.map { it.toString() }
                                        ?: emptyList()
                                topSong = document.getList("topSongs")?.map { it.toString() }
                                    ?: emptyList()
                                album = document.getList("albumArt")?.map { it.toString() }
                                    ?: emptyList()

                            }
                            matchUsers.add(matchUser) // Add to the list of matched users
                        }
                    }

                }

                if (matchUsers.isNotEmpty()) {
                    // Take the first valid user for display (or you can handle them as needed)
                    user = matchUsers[count]


                    Toast.makeText(this, "he: ${matchUsers.count()}", Toast.LENGTH_LONG).show()

                    Toast.makeText(this, "he: ${user.Email}", Toast.LENGTH_LONG).show()

                    // UI and button setup code remains the same
                    val pager = findViewById<ViewPager2>(R.id.imagePager)
                    val name = findViewById<TextView>(R.id.tvName)
                    val pronouns = findViewById<TextView>(R.id.tvPronouns)
                    val albumCover = findViewById<ImageView>(R.id.tvAlbumCover)
                    val songName = findViewById<TextView>(R.id.tvSongName)
                    val artistName = findViewById<TextView>(R.id.tvArtistName)

                    val like = findViewById<FloatingActionButton>(R.id.fab_like)
                    val nope = findViewById<FloatingActionButton>(R.id.fab_nope)
                    val layout = findViewById<ConstraintLayout>(R.id.CLMatch)

                    layout.setOnClickListener {
                        intent = Intent(this, MatchProfile::class.java)
                        intent.putExtra("Email", user.Email)
                        startActivity(intent)
                    }

                    like.setOnClickListener {
                        val currentUserEmail = loggedUser.user?.Email

                        if (currentUserEmail != null) {
                            usersCollection.whereEqualTo("email", user.Email).get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val userDocument = querySnapshot.documents[0]
                                        val userName = userDocument.getString("name")

                                        if (userName != null) {
                                            val likedByData = hashMapOf(
                                                "uid" to currentUserEmail,
                                                "name" to userName
                                            )

                                            userDocument.reference.collection("liked_by")
                                                .add(likedByData)
                                                .addOnSuccessListener {
                                                    loggedUser.shownList.add(user.Email)
                                                    Toast.makeText(
                                                        this,
                                                        "Liked successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    getUsers()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        this,
                                                        "Error adding to liked_by: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "User name not found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error fetching user: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    nope.setOnClickListener {
                        // Add logic for "nope" button if needed
                        loggedUser.shownList.add(user.Email)
                        getUsers()
                    }


                    name.text = user.Name
                    pronouns.text = user.Pronoun
                    songName.text = user.topSong[0]
                    artistName.text = user.topArtist[0]
                    Glide.with(this)
                    .load(user.album[0])
                    .into(albumCover)

                    val imageAdapter = ImagePagerAdapter(user.profilePictureUrl ?: emptyList())
                    pager.adapter = imageAdapter
                }
                else{
                    val pager = findViewById<ViewPager2>(R.id.imagePager)
                    val name = findViewById<TextView>(R.id.tvName)
                    val pronouns = findViewById<TextView>(R.id.tvPronouns)
                    val albumCover = findViewById<ImageView>(R.id.tvAlbumCover)
                    val songName = findViewById<TextView>(R.id.tvSongName)
                    val artistName = findViewById<TextView>(R.id.tvArtistName)

                    Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
                    name.text = "No users left to search thruogh"
                    pronouns.text = ""
                    songName.text = ""
                    artistName.text = ""
                    /*  Glide.with(this)
                    .load(user.album[0])
                    .into(albumCover)*/

                    val imageAdapter = ImagePagerAdapter(user.profilePictureUrl ?: emptyList())
                    pager.adapter = imageAdapter
                }
            }

    }

    private fun DocumentSnapshot.getList(field: String): List<String> {
        return this.get(field) as? List<String> ?: emptyList()
    }
}

class ImagePagerAdapter(private val images: List<String>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.image_pager_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // Assuming you're using Glide or Picasso to load the image URLs
        Glide.with(holder.itemView.context)
            .load(images[position])
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return images.size
    }
}


