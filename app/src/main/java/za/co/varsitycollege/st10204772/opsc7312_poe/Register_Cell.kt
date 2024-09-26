package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register_Cell : AppCompatActivity() {
        var user: User = User()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_cell)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var txtCell = findViewById<EditText>(R.id.edCellNum)
        var btnnext = findViewById<Button>(R.id.btncontinue)

        btnnext.setOnClickListener {
            var usercell = txtCell.text

            user.setUsercell(usercell.toString())

            startActivity(Intent(this, Register_About_You::class.java))
        }

    }
}