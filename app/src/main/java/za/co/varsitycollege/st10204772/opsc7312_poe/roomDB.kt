package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase


@Database(entities = [message::class], version = 1)
abstract class roomDB : RoomDatabase() {
    abstract fun messageDao(): messageDao?

    companion object {
        private var INSTANCE: roomDB? = null

        fun getDatabase(context: Context): roomDB? {
            if (INSTANCE == null) {
                synchronized(roomDB::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = databaseBuilder(
                            context.applicationContext,
                            roomDB::class.java, "app_database"
                        )
                            .fallbackToDestructiveMigration() // Handle migrations if needed
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}