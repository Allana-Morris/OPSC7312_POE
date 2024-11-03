package za.co.varsitycollege.st10204772.opsc7312_poe

import android.app.Application
import com.google.firebase.FirebaseApp

class MusicMatch : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // You can add other global initialization code here
    }
}