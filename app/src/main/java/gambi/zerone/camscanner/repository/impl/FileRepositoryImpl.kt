package gambi.zerone.camscanner.repository.impl

import android.content.Context
import gambi.zerone.camscanner.database.AppDB
import gambi.zerone.camscanner.entity.File
import gambi.zerone.camscanner.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileRepositoryImpl(context: Context): FileRepository {
    val fileDao = AppDB.getInstance(context).fileDao()
    override suspend fun getAllFiles(): List<File> {
        return withContext(Dispatchers.IO){
            fileDao.getAllFiles()
        }
    }

    override suspend fun upsertFile(file: File) {
        withContext(Dispatchers.IO){
            fileDao.upsertFile(file)
        }
    }

    override suspend fun deleteFile(file: File) {
        withContext(Dispatchers.IO){
            fileDao.deleteFile(file)
        }
    }

}