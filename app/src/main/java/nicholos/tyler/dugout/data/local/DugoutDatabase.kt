package nicholos.tyler.dugout.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CachedPlayerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DugoutDatabase : RoomDatabase() {

    abstract fun playerSearchDao(): PlayerSearchDao

    companion object {
        @Volatile
        private var instance: DugoutDatabase? = null

        fun getInstance(context: Context): DugoutDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DugoutDatabase::class.java,
                    "dugout.db"
                ).build().also { instance = it }
            }
        }
    }
}
