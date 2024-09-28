package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Liked_you : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liked_you)

        var navbar = findViewById<BottomNavigationView>(R.id.BNV_Navbar_Liked_You)

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

        val layout: LinearLayout = findViewById(R.id.vert_layout_liked)


        //makes some liked you elements for testing
        //images dont work tho smh
        for ( int in 1..30)
        {
            val inflatedView = LayoutInflater.from(this@Liked_you)
                .inflate(R.layout.layout_liked_you, layout, false)

            layout.addView(inflatedView)
        }


    }
}