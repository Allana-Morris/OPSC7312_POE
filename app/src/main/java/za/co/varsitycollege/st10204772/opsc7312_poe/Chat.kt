package za.co.varsitycollege.st10204772.opsc7312_poe

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val layout: LinearLayout = findViewById(R.id.vert_layout_chat)


        //makes some chat bubbles to test the activity view
        //images dont work tho smh

        for ( int in 1..30)
        {
            val inflatedView = LayoutInflater.from(this@Chat)
                .inflate(R.layout.layout_chat_message_bubble, layout, false)

            layout.addView(inflatedView)
        }


    }
}