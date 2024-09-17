package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Contact : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)
        val layout: LinearLayout = findViewById(R.id.vert_layout_contact)


        //this just spawns like 30 contact elements for testing
        //images dont work tho smh

        for ( int in 1..30)
        {
            val inflatedView = LayoutInflater.from(this@Contact)
                .inflate(R.layout.layout_contact_listing, layout, false)

            layout.addView(inflatedView)
        }
    }
}