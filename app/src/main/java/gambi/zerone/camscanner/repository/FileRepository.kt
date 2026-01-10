package gambi.zerone.camscanner.repository

import gambi.zerone.camscanner.entity.File

interface FileRepository {
    suspend fun getAllFiles(): List<File>
    suspend fun upsertFile(file: File)
    suspend fun deleteFile(file: File)
}