package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class MatchUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var gestureDetector: GestureDetectorCompat
    private var UserSeen: Boolean = false
    private val matchUsers = mutableListOf<MatchUser>()

    var contactDao = roomDB.getDatabase(this)!!.contactDao()!!

    var count = 0
    private var swipedUp = false
    private var swipedDown = false

    // Reference to Firestore
    val firestore = FirebaseFirestore.getInstance()
    val usersCollection = firestore.collection("Users")

    val loggedUserEmail = loggedUser.user?.Email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_ui)
        setupBottomNavigation()
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        val layout = findViewById<ConstraintLayout>(R.id.CLMatch)
        layout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        getUsers()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffY = e2?.y?.minus(e1?.y ?: 0f) ?: 0f

            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY < 0) {
                    // Swipe up - trigger "like"
                    if (!swipedUp) {
                        swipedUp = true
                        handleLike(matchUsers[count])
                    } else {
                        Toast.makeText(
                            this@MatchUI,
                            "Already swiped up on this user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Swipe down - trigger "nope"
                    if (!swipedDown) {
                        swipedDown = true
                        loggedUser.shownList.add(matchUsers[count].Email)
                        Toast.makeText(this@MatchUI, "User passed", Toast.LENGTH_SHORT).show()
                        getUsers()
                    } else {
                        Toast.makeText(
                            this@MatchUI,
                            "Already swiped down on this user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return true
            }
            return false
        }
    }

    fun CheckUserUnseen(matchemail: String?) {
        UserSeen = false
        for (seenuser in loggedUser.shownList) {
            if (seenuser == matchemail) {
                UserSeen = true
                break
            }
        }
    }


    private fun getUsers() {
        lifecycleScope.launch {
            // Fetch contacts asynchronously
            val contactList = contactDao.getAllContacts().map { it.email }

            usersCollection.whereNotEqualTo("albumArt", null).limit(5)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val userEmail = document.getString("email")

                        // Only proceed if the user email is not the logged-in user's email
                        if (userEmail != loggedUserEmail) {
                            CheckUserUnseen(userEmail)

                            // Exclude users who are already in contacts and not yet shown
                            if (!UserSeen && !contactList.contains(userEmail)) {
                                val matchUser = MatchUser().apply {
                                    Name = document.getString("name") ?: ""
                                    Age = document.getLong("age")?.toInt() ?: 0
                                    Email = userEmail ?: ""
                                    Gender = document.getString("gender") ?: ""
                                    Pronoun = document.getString("pronoun") ?: ""
                                    profilePictureUrl = document.getList("profileImageUrls")
                                    topGenre = document.getList("topGenres")?.map { it.toString() } ?: emptyList()
                                    topArtist = document.getList("topArtists")?.map { it.toString() } ?: emptyList()
                                    topSong = document.getList("topSongs")?.map { it.toString() } ?: emptyList()
                                    album = document.getList("albumArt")?.map { it.toString() } ?: emptyList()
                                }
                                matchUsers.add(matchUser)
                            }
                        }
                    }

                    // Display the next match or a "No Users Found" message
                    if (matchUsers.isNotEmpty()) {
                        val user = matchUsers[count]
                        swipedUp = false
                        swipedDown = false
                        updateUserUI(user)
                    } else {
                        displayNoUsersFoundUI()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@MatchUI, "Error fetching users: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun updateUserUI(user: MatchUser) {
        val pager = findViewById<ViewPager2>(R.id.imagePager)
        val name = findViewById<TextView>(R.id.tvName)
        val pronouns = findViewById<TextView>(R.id.tvPronouns)
        val albumCover = findViewById<ImageView>(R.id.tvAlbumCover)
        val songName = findViewById<TextView>(R.id.tvSongName)
        val artistName = findViewById<TextView>(R.id.tvArtistName)

        name.text = user.Name
        pronouns.text = user.Pronoun
        songName.text = user.topSong.firstOrNull() ?: "Unknown Song"
        artistName.text = user.topArtist.firstOrNull() ?: "Unknown Artist"

        user.album.firstOrNull()?.let {
            Glide.with(this).load(it).into(albumCover)
        } ?: albumCover.setImageDrawable(null)

        val imageAdapter = ImagePagerAdapter(user.profilePictureUrl ?: emptyList())
        pager.adapter = imageAdapter
    }

    private fun handleLike(user: MatchUser) {
        loggedUser.user?.Email?.let { currentUserEmail ->
            usersCollection.whereEqualTo("email", user.Email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val userDocument = querySnapshot.documents[0]
                        val userName = userDocument.getString("name")

                        if (userName != null) {
                            // Check if the logged-in user is already in the liked_by collection
                            userDocument.reference.collection("liked_by")
                                .whereEqualTo("uid", currentUserEmail)
                                .get()
                                .addOnSuccessListener { likedBySnapshot ->
                                    if (likedBySnapshot.isEmpty) {
                                        // User is not already liked, so proceed to add
                                        val likedByData = hashMapOf("uid" to currentUserEmail, "name" to userName)
                                        userDocument.reference.collection("liked_by").add(likedByData)
                                            .addOnSuccessListener {
                                                loggedUser.shownList.add(user.Email)
                                                Toast.makeText(this, "Liked successfully", Toast.LENGTH_SHORT).show()
                                                getUsers()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error adding to liked_by: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        // User already liked, skip adding
                                        Toast.makeText(this, "User already liked", Toast.LENGTH_SHORT).show()
                                        getUsers()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error checking liked_by: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "User name not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "User email is not available", Toast.LENGTH_SHORT).show()
    }


    private fun displayNoUsersFoundUI() {
        val pager = findViewById<ViewPager2>(R.id.imagePager)
        val name = findViewById<TextView>(R.id.tvName)
        val pronouns = findViewById<TextView>(R.id.tvPronouns)
        val albumCover = findViewById<ImageView>(R.id.tvAlbumCover)
        val songName = findViewById<TextView>(R.id.tvSongName)
        val artistName = findViewById<TextView>(R.id.tvArtistName)

        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
        name.text = "No users left to search through"
        pronouns.text = ""
        songName.text = ""
        artistName.text = ""
        albumCover.setImageDrawable(null)

        pager.adapter = ImagePagerAdapter(emptyList())
    }

    private fun DocumentSnapshot.getList(field: String): List<String> {
        return this.get(field) as? List<String> ?: emptyList()
    }

    private fun setupBottomNavigation() {
        val navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Match)
        navbar.selectedItemId = R.id.nav_match
        navbar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_match -> startActivity(Intent(this, MatchUI::class.java))
                R.id.nav_like -> startActivity(Intent(this, Liked_you::class.java))
                R.id.nav_chat -> startActivity(Intent(this, Contact::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileUI::class.java))
                else -> false
            }
            true
        }
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


