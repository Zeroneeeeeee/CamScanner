package gambi.zerone.camscanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gambi.zerone.camscanner.dao.FileDao
import gambi.zerone.camscanner.entity.File

@Database(entities = [File::class], version = 1)
abstract class AppDB : RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null
        fun getInstance(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java, "scanner.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }

    }
}