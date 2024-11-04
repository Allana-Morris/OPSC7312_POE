package za.co.varsitycollege.st10204772.opsc7312_poe

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Register_About_You : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_about_you)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edName = findViewById<EditText>(R.id.edName)
        val edDOB = findViewById<EditText>(R.id.edDOB)
        val rdgGender = findViewById<RadioGroup>(R.id.rdgGender)
        val spPronouns = findViewById<Spinner>(R.id.spnPronouns)
        val btnContinue = findViewById<Button>(R.id.btnContinueAbout)
        val txtMore = findViewById<TextView>(R.id.txtMoreGender)
        val spnMoreGenders = findViewById<Spinner>(R.id.spGender)
        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            // Set the selected date on the calendar
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Format the date and display it in the EditText
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            edDOB.setText(dateFormat.format(calendar.time))
        }

        // Load gender and pronoun data
        val genderItems = resources.getStringArray(R.array.xtragenders)
        val pronounItems = resources.getStringArray(R.array.pronoun_array)
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderItems)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, pronounItems)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnMoreGenders.adapter = adapter1
        spPronouns.adapter = adapter2

        rdgGender.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rdbInvalid -> showExtras(txtMore, spnMoreGenders)
                else -> hideExtras(txtMore, spnMoreGenders)
            }
        }

        edDOB.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                DatePickerDialog(
                    this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            true
        }

        btnContinue.setOnClickListener {
            val userName = edName.text.toString()
            val userDob = edDOB.text.toString()
            var userGender = ""
            var subGender: String? = null
            val userSelect = rdgGender.checkedRadioButtonId
            val userPronouns = spPronouns.selectedItem.toString()

            when (userSelect) {
                R.id.rdbWoman -> userGender = "Woman"
                R.id.rdbMan -> userGender = "Man"
                R.id.rdbInvalid -> {
                    userGender = "NonBinary"
                    subGender = spnMoreGenders.selectedItem.toString()
                }
            }

            val userAge: Int = calcAge(userDob)
            if (userAge == -1) {
                Log.e(TAG, "Age Calculation Error")
                return@setOnClickListener
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            var userDOB = Date()

            try {
                userDOB = dateFormat.parse(userDob)!!
            } catch (e: ParseException) {
                Log.e(TAG, "Parse Exception")
                return@setOnClickListener
            }

            if (userAge < 18) {
                Toast.makeText(this, "You must be 18 or older to register.", Toast.LENGTH_LONG).show()
            } else {
                saveUserProfile(userName, userDOB, userAge, userGender, subGender, userPronouns)
            }
        }
    }

    private fun saveUserProfile(name: String, dob: Date, age: Int, gender: String, genderLabel: String?, pronouns: String) {
        val userEmail = loggedUser.user?.Email
        Toast.makeText(this, "check this: " + loggedUser.user?.Email.toString(), Toast.LENGTH_LONG).show()

        if (userEmail == null) {
            Log.e(TAG, "Error: User not authenticated")
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Query for the user document where the "email" field equals userEmail
        db.collection("Users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Get the first matching document
                    val userDoc = querySnapshot.documents.first()

                    val userProfileData = mapOf(
                        "name" to name,
                        "dob" to dob,
                        "age" to age,
                        "gender" to gender,
                        "genderLabel" to genderLabel,
                        "pronoun" to pronouns
                    )

                    // Update the user document
                    userDoc.reference.update(userProfileData)
                        .addOnSuccessListener {
                            Log.d(TAG, "User profile updated successfully")
                            startActivity(Intent(this, Register_Image_Upload::class.java))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating user profile", e)
                        }
                } else {
                    Log.e(TAG, "No user found with email: $userEmail")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error retrieving user document", e)
            }
    }


    private fun showExtras(txtMore: TextView, spnMoreGenders: Spinner) {
        txtMore.visibility = VISIBLE
        spnMoreGenders.visibility = VISIBLE
    }

    private fun hideExtras(txtMore: TextView, spnMoreGenders: Spinner) {
        txtMore.visibility = INVISIBLE
        spnMoreGenders.visibility = INVISIBLE
    }

    private fun calcAge(dob: String): Int {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val birthday: Date = try {
            dateFormat.parse(dob) ?: return -1
        } catch (e: ParseException) {
            return -1
        }

        val currentDate = Calendar.getInstance()
        val birthdayCalendar = Calendar.getInstance()
        birthdayCalendar.time = birthday

        var age = currentDate.get(Calendar.YEAR) - birthdayCalendar.get(Calendar.YEAR)

        if (currentDate.get(Calendar.MONTH) < birthdayCalendar.get(Calendar.MONTH) ||
            (currentDate.get(Calendar.MONTH) == birthdayCalendar.get(Calendar.MONTH) &&
                    currentDate.get(Calendar.DAY_OF_MONTH) < birthdayCalendar.get(Calendar.DAY_OF_MONTH))
        ) {
            age--
        }

        return age
    }
}
