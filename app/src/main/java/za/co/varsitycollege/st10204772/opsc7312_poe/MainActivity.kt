import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10204772.opsc7312_poe.ContactDao
import za.co.varsitycollege.st10204772.opsc7312_poe.R
import za.co.varsitycollege.st10204772.opsc7312_poe.StartActivity
import za.co.varsitycollege.st10204772.opsc7312_poe.messageDao
import za.co.varsitycollege.st10204772.opsc7312_poe.roomDB

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Start the next activity
        val intent = Intent(this@MainActivity, StartActivity::class.java)
        startActivity(intent)
    }
}
