package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        // Populate Gender Spinner
        val genderSpinner: Spinner = findViewById(R.id.spinnerGender)
        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.xtragenders,  // Replace with gender array from genders.xml
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Populate Music Genre Spinner
        val musicGenreSpinner: Spinner = findViewById(R.id.spinnerMusicGenre)
        val musicGenreAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.music_genre_options,  // From strings.xml
            android.R.layout.simple_spinner_item
        )
        musicGenreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        musicGenreSpinner.adapter = musicGenreAdapter

        // Populate Location Spinner
        val locationSpinner: Spinner = findViewById(R.id.spinnerLocation)
        val locationAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.location_options,  // Assuming locations are defined in strings.xml
            android.R.layout.simple_spinner_item
        )
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = locationAdapter

        // Save Changes Button OnClickListener in FilterActivity
        findViewById<Button>(R.id.btnSaveChanges).setOnClickListener {
            val selectedGender = genderSpinner.selectedItem.toString()
            val selectedGenre = musicGenreSpinner.selectedItem.toString()
            val selectedLocation = locationSpinner.selectedItem.toString()

            val resultIntent = Intent().apply {
                putExtra("selectedGender", selectedGender)
                putExtra("selectedGenre", selectedGenre)
                putExtra("selectedLocation", selectedLocation)  // Pass the selected location as well
            }
            setResult(RESULT_OK, resultIntent)
            finish()  // Close FilterActivity and return to MatchUI
        }
    }
}
