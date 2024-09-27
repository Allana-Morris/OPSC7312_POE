package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Register_About_You : AppCompatActivity() {
    var edName = findViewById<EditText>(R.id.edName)
    var edDOB = findViewById<EditText>(R.id.edDOB)
    var rdgGender = findViewById<RadioGroup>(R.id.rdgGender)
    var spPronouns = findViewById<Spinner>(R.id.spnPronouns)
    var btncontinue = findViewById<Button>(R.id.btnContinueAbout)
    var txtMore = findViewById<TextView>(R.id.txtMoreGender)
    var spnmoregenders = findViewById<Spinner>(R.id.spGender)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_about_you)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Loads data from genders.xml & pronouns.xml into each spinner
        val genderitems = resources.getStringArray(R.array.XtraGenders)
        val pronounitems = resources.getStringArray(R.array.pronoun_array)
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderitems)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, pronounitems)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnmoregenders.adapter = adapter1
        spPronouns.adapter = adapter2


        //Shows hidden Components
        rdgGender.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rdbInvalid -> showExtras()
                else -> hideExtras()
            }
        }

        btncontinue.setOnClickListener {
            //data capture
            val userName = edName.text.toString()
            val userdob = edDOB.text.toString()
            var userGender = ""
            var subGender: String? = null
            val userselect = rdgGender.checkedRadioButtonId
            val userPronouns = spPronouns.selectedItem.toString()

            when (userselect) {
                R.id.rdbWoman -> userGender = "Woman"
                R.id.rdbMan -> userGender = "Man"
                R.id.rdbInvalid -> {
                    userGender = "NonBinary"
                    subGender = spnmoregenders.selectedItem.toString()
                }
            }




            val userAge: Int = calcAge(userdob)
            when (userAge){
                -1 -> Log.e(TAG, "Age Calculation Error")
            }

            val user = User()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            var userDOB = Date()

             try {
                userDOB = dateFormat.parse(userdob)!!
             } catch(e: ParseException){
                 Log.e(TAG, "Parse Exception")
             }

            if (userAge < 18) {
                //error
            } else {
                user.Name = userName
                user.DOB = userDOB
                user.Age = userAge
                user.Gender = userGender
                user.GenderLabel = subGender
                user.Pronoun = userPronouns
                Log.e(TAG, "Success")
                startActivity(Intent(this, Register_Image_Upload::class.java))
            }
        }
    }

    private fun showExtras() {
        txtMore.visibility = VISIBLE
        spnmoregenders.visibility = VISIBLE
    }

    private fun hideExtras() {
        txtMore.visibility = INVISIBLE
        spnmoregenders.visibility = INVISIBLE
    }

    private fun calcAge(dob: String): Int {
        // Define the date format
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Parse the input date
        val birthday: Date = dateFormat.parse(dob) ?: return -1 // return -1 if parsing fails

        // Get the current date
        val currentDate = Calendar.getInstance()

        // Create a Calendar instance for the birthday
        val birthdayCalendar = Calendar.getInstance()
        birthdayCalendar.time = birthday

        // Calculate the age
        var age = currentDate.get(Calendar.YEAR) - birthdayCalendar.get(Calendar.YEAR)

        // Adjust age if the birthday hasn't occurred yet this year
        if (currentDate.get(Calendar.MONTH) < birthdayCalendar.get(Calendar.MONTH) ||
            (currentDate.get(Calendar.MONTH) == birthdayCalendar.get(Calendar.MONTH) &&
                    currentDate.get(Calendar.DAY_OF_MONTH) < birthdayCalendar.get(Calendar.DAY_OF_MONTH))
        ) {
            age--
        }

        return age
    }
}