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

data class PdfFile(val uri: Uri, val name: String)

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
                MediaStore.Files.FileColumns.DATA // Một số máy cũ cần cột này để lấy tên
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
                    val dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)

                        // Thử lấy tên từ DISPLAY_NAME, nếu không có thì lấy từ đường dẫn DATA
                        var name = if (nameColumn != -1) cursor.getString(nameColumn) else null
                        if (name.isNullOrEmpty() && dataColumn != -1) {
                            val path = cursor.getString(dataColumn)
                            name = path?.substringAfterLast('/')
                        }

                        val contentUri = ContentUris.withAppendedId(collection, id)

                        // Nếu vẫn Unknown, dùng hàm getFileName bổ trợ
                        val finalName = name ?: getFileName(contentUri)

                        tempList.add(PdfFile(contentUri, finalName))
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