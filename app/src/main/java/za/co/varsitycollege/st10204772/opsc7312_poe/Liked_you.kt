package za.co.varsitycollege.st10204772.opsc7312_poe

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Liked_you : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liked_you)

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