package za.co.varsitycollege.st10204772.opsc7312_poe

import android.text.Editable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class InputValidation {

    fun isStringInput(input: Editable): Boolean{
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

    fun formatPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.startsWith("0")) {
            // Replace leading '0' with the country code prefix
            "+27 ${phoneNumber.substring(2)}"

        } else {
            phoneNumber
        }
    }



}