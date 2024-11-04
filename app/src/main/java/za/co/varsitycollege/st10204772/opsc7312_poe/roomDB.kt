package za.co.varsitycollege.st10204772.opsc7312_poe

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocalUser::class, Message::class, Cont::class], version = 2)
abstract class roomDB : RoomDatabase() {
    abstract fun localUserDao(): LocalUserDao
    abstract fun messageDao(): messageDao?
    abstract fun contactDao(): ContactDao?

    companion object {
        @Volatile
        private var INSTANCE: roomDB? = null

        fun getDatabase(context: Context): roomDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    roomDB::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Handle migrations if needed
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
