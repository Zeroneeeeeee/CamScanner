package gambi.zerone.camscanner.view.files

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PdfFile(
    val uri: Uri,
    val name: String,
    val size: Long,        // bytes
    val dateAdded: Long    // millis
)

class FilesViewModel(application: Application) : AndroidViewModel(application) {
    var pdfFiles = mutableStateListOf<PdfFile>()
        private set

    // Hàm lấy tên file từ Uri (An toàn hơn cho Android 10+)
    private fun getFileName(uri: Uri): String {
        var name = "Unknown.pdf"
        val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    fun loadPdfFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val tempList = mutableListOf<PdfFile>()

            // Sử dụng Files.getContentUri để quét toàn bộ các loại file
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            // Lấy ID và DISPLAY_NAME
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA // fallback cho máy cũ
            )


            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("application/pdf")

            try {
                getApplication<Application>().contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val nameColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val dateAddedColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)

                        var name = if (nameColumn != -1) cursor.getString(nameColumn) else null
                        if (name.isNullOrEmpty() && dataColumn != -1) {
                            val path = cursor.getString(dataColumn)
                            name = path?.substringAfterLast('/')
                        }

                        val size = if (sizeColumn != -1) cursor.getLong(sizeColumn) else 0L

                        val dateAddedSeconds =
                            if (dateAddedColumn != -1) cursor.getLong(dateAddedColumn) else 0L
                        val dateAddedMillis = dateAddedSeconds * 1000

                        val contentUri = ContentUris.withAppendedId(collection, id)
                        val finalName = name ?: getFileName(contentUri)

                        tempList.add(
                            PdfFile(
                                uri = contentUri,
                                name = finalName,
                                size = size,
                                dateAdded = dateAddedMillis
                            )
                        )
                    }

                }

                withContext(Dispatchers.Main) {
                    pdfFiles.clear()
                    pdfFiles.addAll(tempList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun Long.formatFileSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb /1024.0
    return when {
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        gb>=1 -> String.format("%.2f GB", gb)
        else -> "$this B"
    }
}

fun Long.formatDate(): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}

fun Long.formatTime(): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}