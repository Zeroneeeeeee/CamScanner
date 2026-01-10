package gambi.zerone.camscanner.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import gambi.zerone.camscanner.entity.File

@Dao
interface FileDao {
    @Upsert
    suspend fun upsertFile(file: File)
    @Delete
    suspend fun deleteFile(file: File)
    @Query("SELECT * FROM files")
    suspend fun getAllFiles(): List<File>
}