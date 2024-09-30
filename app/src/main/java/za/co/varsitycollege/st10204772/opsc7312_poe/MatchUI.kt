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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class MatchUI : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    private val TAG = "MatchUI"

    // Store user's top songs, artists, and genres
    private lateinit var currentUserTopSongs: List<String>
    private lateinit var currentUserTopArtists: List<String>
    private lateinit var currentUserTopGenres: List<String>

    // Variables to hold filter data
    private var selectedGender: String? = null
    private var selectedGenre: String? = null
    private var selectedLocation: String? = null
    private lateinit var profileImages: MutableList<Bitmap>

    private var UserSeen: Boolean = false


    private val SPOTIFY_AUTH_REQUEST_CODE = 1001


    // Register a result launcher for the filter activity
    /*   private val filterResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedGender = data?.getStringExtra("selectedGender")
            selectedGenre = data?.getStringExtra("selectedGenre")
            selectedLocation = data?.getStringExtra("selectedLocation")  // Get the selected location

            if (selectedGender == null || selectedGenre == null || selectedLocation == null) {
                Log.e(TAG, "Invalid filter data received: gender=$selectedGender, genre=$selectedGenre, location=$selectedLocation")
                Toast.makeText(this, "Invalid filters selected", Toast.LENGTH_SHORT).show()
            } else {
                // Apply filtering based on the selected gender, genre, and location
                //fetchFilteredProfiles()
            }
        }
    }*/

    //these need to be set under setcontentview if you're gonna use em again
    /*val sStorage = SecureStorage(this)
  val CLIENT_ID = sStorage.getID("CLIENT_ID")
  val REDIRECT_URI = sStorage.getID("REDIRECT_URI")
  var spotifyAccessToken: String? = sStorage.getID("ACCESS_TOKEN")
  val AUTH_URL = "https://accounts.spotify.com/authorize?client_id=$CLIENT_ID&response_type=token&redirect_uri=$REDIRECT_URI&scope=user-top-read"*/

    //  @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_ui)

        var count = 0;

        // Reference to Firestore
        val firestore = FirebaseFirestore.getInstance()
        val usersCollection = firestore.collection("Users")

        // Get the logged-in user's email
        val loggedUserEmail = loggedUser.user?.Email

// Query to get users with a specific spotifyId
        fun getUsers(){
        usersCollection.whereNotEqualTo("spotifyId", null).limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                var user = MatchUser();
                val matchUsers = mutableListOf<MatchUser>() // List to hold matched users

                for (document in querySnapshot.documents) {
                    // Filter out documents where email matches the logged-in user
                    val userEmail = document.getString("email")
                    if (userEmail != loggedUserEmail) {
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
                        matchUsers.add(matchUser) // Add to the list of matched users
                    }

                }

                if (matchUsers.isNotEmpty()) {
                    // Take the first valid user for display (or you can handle them as needed)
                     user = matchUsers[count]
                }

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
                        usersCollection.whereEqualTo("email", currentUserEmail).get()
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
                                                Toast.makeText(
                                                    this,
                                                    "Liked successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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
                }

               /* name.text = user.Name
                pronouns.text = user.Pronoun
                songName.text = user.topSong[0]
                artistName.text = user.topArtist[0]
                Glide.with(this)
                    .load(user.album[0])
                    .into(albumCover)

                val imageAdapter = ImagePagerAdapter(user.profilePictureUrl ?: emptyList())
                pager.adapter = imageAdapter*/
            }
    }
        }

    private fun DocumentSnapshot.getList(field: String): List<String> {
        return this.get(field) as? List<String> ?: emptyList()
    }
}


/*

// Trigger FilterActivity
findViewById<ImageView>(R.id.iV_Filter).setOnClickListener {
    val intent = Intent(this, FilterActivity::class.java)
    filterResultLauncher.launch(intent)
}

// Initialize Firestore instance
db = FirebaseFirestore.getInstance()

// Fetch user data (Name, Age, Pronouns) from Firestore
fetchUserDetails()

// Fetch top 3 songs from Spotify
spotifyAccessToken?.let { token ->
    fetchTopSongsFromSpotify(token)
}

// Set an onClickListener on the profile picture to navigate to ProfileUI
val profilePic = findViewById<FloatingActionButton>(R.id.fab_profile)
profilePic.setOnClickListener {
    val intent = Intent(this, MatchProfile::class.java)
    // Pass any additional data if needed (e.g., user ID)
    intent.putExtra("AccessToken", spotifyAccessToken)
    startActivity(intent)
}

// Set onClickListeners for Floating Action Buttons
findViewById<FloatingActionButton>(R.id.fab_nope).setOnClickListener {
    // Handle Nope: Fetch and display the next user
    fetchNextUser()
}

findViewById<FloatingActionButton>(R.id.fab_like).setOnClickListener {
    // Handle Like: Check for match based on top 3 songs
    checkForMatch()
}

}
// Method to retrieve the Spotify access token
private fun getSpotifyAccessToken() {
val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL))
startActivityForResult(intent, SPOTIFY_AUTH_REQUEST_CODE)
}

// Handle the result in onActivityResult
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
super.onActivityResult(requestCode, resultCode, data)
if (requestCode == SPOTIFY_AUTH_REQUEST_CODE) {
    val uri = data?.data
    if (uri != null && REDIRECT_URI?.let { uri.toString().startsWith(it) } == true) {
        val token = uri.getFragment()?.split("&")?.firstOrNull { it.startsWith("access_token=") }
            ?.substringAfter("access_token=")
        if (token != null) {
            spotifyAccessToken = token
            Log.d(TAG, "Access token retrieved: $spotifyAccessToken")
            // Now you can call methods that require the access token
        }
    }
}
}

// Fetch profiles based on selected filters
private fun fetchFilteredProfiles() {
val query = db.collection("users")

// Apply gender filter
selectedGender?.let { gender ->
    query.whereEqualTo("gender", gender)
}

// Apply genre filter
selectedGenre?.let { genre ->
    query.whereArrayContains("favoriteGenres", genre)
}

// Apply location filter
selectedLocation?.let { location ->
    query.whereEqualTo("location", location)
}

      query.get()
          .addOnSuccessListener { documents ->
              if (documents.isEmpty) {
                  Toast.makeText(
                      this,
                      "No profiles found with selected filters",
                      Toast.LENGTH_SHORT
                  ).show()
                  Log.d(TAG, "No profiles found")
              } else {
                  for (document in documents) {
                      Log.d(TAG, "Found profile: ${document.data}")
                      // Handle displaying of profiles here
                  }
                  // Adjust Firestore query to include gender and genre filters
                  selectedGenre?.let {
                      db.collection("Users")
                          .whereEqualTo("Gender", selectedGender)
                          .whereArrayContains("favoriteGenres", it)
                          .get()
                          .addOnSuccessListener { documents ->
                              // Handle profile loading and display
                          }
                  }
                      ?.addOnFailureListener { exception ->
                          Log.d(TAG, "Error fetching profiles with filters: ", exception)
                          Toast.makeText(
                              this,
                              "Error loading filtered profiles",
                              Toast.LENGTH_SHORT
                          ).show()
                      }
              }
          }
          */


/* private fun fetchUserDetails() {
     // Assuming Firestore stores user's name, age, and pronouns
      db.collection("Users").document("User_id").get()
         .addOnSuccessListener { document ->
             if (document != null) {
                 val userName = document.getString("Name")
                 val userAge = document.getLong("Age")?.toString() ?: ""
                 val userPronouns = document.getString("Pronouns")

                 // Display user data in respective fields
                 findViewById<TextView>(R.id.tvName).text = "$userName, $userAge"
                 findViewById<TextView>(R.id.tvPronouns).text = userPronouns
                 DatabaseReadandWrite().loadProfileImages("$userName", this ) { images ->
                     if (images.isNotEmpty()) {
                         profileImages = images.toMutableList()
                     } else {
                         // Handle the case where no images were loaded
                         Log.d(TAG, "No images found")
                     }
                 }
                 // Load your bitmap images into the list (replace with your actual loading logic)
                 for (i in 0 until minOf(6, profileImages.size)){
                 profileImages.add(profileImages[i])}
                 // Find the ViewPager2 and set the adapter
                 val viewPager = findViewById<ViewPager2>(R.id.imagePager)
                 val adapter = ProfileImageAdapter(profileImages)
                 viewPager.adapter = adapter
             } else {
                 Log.d(TAG, "No such document")
             }
         }
         .addOnFailureListener { exception ->
             Log.d(TAG, "Error getting user details: ", exception)
         }
 }

 private fun fetchTopSongsFromSpotify(accessToken: String) {
     val request = okhttp3.Request.Builder()
         .url("https://api.spotify.com/v1/me/top/tracks?limit=1") // Fetch only the top song
         .addHeader("Authorization", "Bearer $accessToken")
         .build()

     OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
         override fun onFailure(call: okhttp3.Call, e: IOException) {
             Log.e(TAG, "Error fetching top song: $e")
         }

         override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
             response.body?.let {
                 val jsonResponse = JSONObject(it.string())
                 val topTrack = jsonResponse.getJSONArray("items").getJSONObject(0)

                 // Extract song name, artist name, and album cover
                 val songName = topTrack.getString("name")
                 val artistName = topTrack.getJSONArray("artists").getJSONObject(0).getString("name")
                 val albumCoverUrl = topTrack.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url")

                 // Update UI with the fetched details
                 runOnUiThread {
                     findViewById<TextView>(R.id.tvSongName).text = songName
                     findViewById<TextView>(R.id.tvArtistName).text = artistName

                     // Load the album cover into ImageView
                     val albumCoverImageView = findViewById<ImageView>(R.id.tvAlbumCover)
                     Glide.with(this@MatchUI)
                         .load(albumCoverUrl)
                         .into(albumCoverImageView)
                 }
             }
         }
     })
 }


 private fun fetchNextUser() {
     // Fetch and display the next user from Firestore
     db.collection("Users").document("next_user_id").get()
         .addOnSuccessListener { document ->
             if (document != null) {
                 val userName = document.getString("name")
                 val userAge = document.getLong("age")?.toString() ?: ""
                 val userPronouns = document.getString("pronouns")
                 val profileImageUrls = document.get("profileImageUrls") as? List<String>  // Fetch image URLs list
                 val topSongName = document.getString("topSongName") // Assuming Firestore has the top song name
                 val topSongAlbumCoverUrl = document.getString("topSongAlbumCoverUrl") // Assuming Firestore has the album cover URL

                 // Update the UI with new user's details
                 findViewById<TextView>(R.id.tvName).text = "$userName, $userAge"
                 findViewById<TextView>(R.id.tvPronouns).text = userPronouns
                 DatabaseReadandWrite().loadProfileImages("$userName", this ) { images ->
                     if (images.isNotEmpty()) {
                         profileImages = images.toMutableList()
                     } else {
                         // Handle the case where no images were loaded
                         Log.d(TAG, "No images found")
                     }
                 }
                 // Load your bitmap images into the list (replace with your actual loading logic)
                 for (i in 0 until minOf(6, profileImages.size)){
                     profileImages.add(profileImages[i])}
                 // Find the ViewPager2 and set the adapter
                 val viewPager = findViewById<ViewPager2>(R.id.imagePager)
                 val adapter = ProfileImageAdapter(profileImages)
                 viewPager.adapter = adapter
                 findViewById<TextView>(R.id.tvSongName).text = topSongName ?: "No song available"

                 // Load the album cover using Glide, if available
                 val albumCoverImageView = findViewById<ImageView>(R.id.tvAlbumCover)
                 if (!topSongAlbumCoverUrl.isNullOrEmpty()) {
                     Glide.with(this)
                         .load(topSongAlbumCoverUrl)  // Load the album cover image
                         .into(albumCoverImageView)
                 } else {
                     albumCoverImageView.setImageResource(R.drawable.albumimage) // Placeholder for album cover
                 }
             } else {
                 Log.d(TAG, "No such document")
             }
         }
         .addOnFailureListener { exception ->
             Log.d(TAG, "Error getting next user: ", exception)
         }
 }

 private fun checkForMatch() {
     spotifyAccessToken?.let { token ->
         val request = Request.Builder()
             .url("https://api.spotify.com/v1/me/top/tracks?limit=3")
             .addHeader("Authorization", "Bearer $token")
             .build()

         OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
             override fun onFailure(call: okhttp3.Call, e: IOException) {
                 Log.e(TAG, "Error fetching top songs: $e")
             }

             override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                 response.body?.let {
                     val jsonResponse = JSONObject(it.string())
                     val topTracks = jsonResponse.getJSONArray("items")

                     // Extract top songs and artists of the viewed user
                     val viewedUserTopSongs = List(topTracks.length()) { i ->
                         topTracks.getJSONObject(i).getString("name")
                     }
                     val viewedUserTopArtists = List(topTracks.length()) { i ->
                         topTracks.getJSONObject(i)
                             .getJSONArray("artists").getJSONObject(0).getString("name")
                     }

                     // Fetch genres for the viewed user's artists and store them
                     val viewedUserTopGenres = mutableSetOf<String>()

                     val genreFetchers = viewedUserTopArtists.map { artistName ->
                         fetchArtistGenres(token, artistName, viewedUserTopGenres)
                     }

                     // Wait for all genre fetching requests to complete
                     genreFetchers.forEach { it.join() }

                     runOnUiThread {
                         // Check for matches in songs, artists, and genres
                         val commonSongs = currentUserTopSongs.intersect(viewedUserTopSongs)
                         val commonArtists = currentUserTopArtists.intersect(viewedUserTopArtists)
                         val commonGenres = currentUserTopGenres.intersect(viewedUserTopGenres)

                         if (commonSongs.isNotEmpty() || commonArtists.isNotEmpty() || commonGenres.isNotEmpty()) {
                             // If there's a match, show a message
                             Toast.makeText(
                                 this@MatchUI,
                                 "It's a match! Common songs: $commonSongs, artists: $commonArtists, genres: $commonGenres",
                                 Toast.LENGTH_LONG
                             ).show()
                         } else {
                             Toast.makeText(this@MatchUI, "No match found", Toast.LENGTH_SHORT).show()
                         }
                     }
                 }
             }
         })
     }

        var navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Match)

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

 // Helper function to fetch artist genres from Spotify API
 private fun fetchArtistGenres(accessToken: String, artistName: String, viewedUserTopGenres: MutableSet<String>): Thread {
     val thread = Thread {
         val request = okhttp3.Request.Builder()
             .url("https://api.spotify.com/v1/search?q=$artistName&type=artist&limit=1")
             .addHeader("Authorization", "Bearer $accessToken")
             .build()

         OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
             override fun onFailure(call: okhttp3.Call, e: IOException) {
                 Log.e(TAG, "Error fetching artist genres: $e")
             }

             override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                 response.body?.let {
                     val jsonResponse = JSONObject(it.string())
                     val artists = jsonResponse.getJSONObject("artists").getJSONArray("items")

                     if (artists.length() > 0) {
                         val artist = artists.getJSONObject(0)
                         val genres = artist.getJSONArray("genres")
                         for (i in 0 until genres.length()) {
                             viewedUserTopGenres.add(genres.getString(i))  // Add each genre to the set
                         }
                     }
                 }
             }
         })
     }
     thread.start()
     return thread
 }

    */
   class ImagePagerAdapter(private val images: List<String>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

       class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
           val imageView: ImageView = itemView.findViewById(R.id.imageView)
       }

       override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
           val view = LayoutInflater.from(parent.context).inflate(R.layout.image_pager_item, parent, false)
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


