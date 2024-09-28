package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.ContentValues.TAG
import android.text.Editable
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class InputValidation {

    fun isStringInput(input: String): Boolean{
        return if (!input.isNullOrEmpty()){
            true
        } else {
            return false
        }
    }

    fun isString(input: String?): Boolean{
        return if (!input.isNullOrEmpty()){
            true
        } else {
            return false
        }
    }

    fun isDOB(input: String?): Boolean{
        val sdf = SimpleDateFormat(/* pattern = */ "dd/MM/yyyy", /* locale = */ Locale.getDefault())
        sdf.isLenient = false // Set lenient to false to strictly validate dates
        return try {
            input?.let { sdf.parse(it) } != null // If parse succeeds, the date is valid
        } catch (e: ParseException) {
            false // If parse fails, the date is invalid
        }
    }

    fun isEmail(input: String): Boolean{
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$".toRegex()

        return input.matches(emailPattern)

    }

    fun isPassword(input: String): Boolean{
        // Regular expression to check the conditions:
        // ^ asserts start of string
        // (?=.*[A-Z]) requires at least one uppercase letter
        // (?=.*[a-z]) requires at least one lowercase letter (if needed)
        // (?=.*[0-9]) requires at least one digit
        // (?=.*[@#$%^&+=!]) requires at least one special character
        // .{8,} requires at least 12 characters
        // $ asserts end of string
        val passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#\$%^&+=!])(?=.{8,}).*$".toRegex()

        return input.matches(passwordPattern)
    }



}