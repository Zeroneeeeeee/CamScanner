package gambi.zerone.camscanner.view.scanner

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import gambi.zerone.camscanner.entity.File
import androidx.core.net.toUri

data class FileVM(
    val uri: Uri,
    val name: String,
    val size: Double,
    val timeStamp:Long = System.currentTimeMillis(),
)

fun FileVM.toEntity() = File(
    id = timeStamp,
    uri = uri.toString(),
    name = name,
    size = size,
)

fun File.toVM() = FileVM(
    uri = uri.toUri(),
    name = name,
    size = size,
    timeStamp = id
)

fun Context.getFileSize(uri: Uri): Long {
    val projection = arrayOf(OpenableColumns.SIZE)

    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index != -1) {
                return cursor.getLong(index)
            }
        }
    }
    return -1L
}

fun Long.formatFileSize(): String {
    if (this <= 0) return "0 B"
    val kb = this/ 1024f
    val mb = kb / 1024f

    return when {
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$this B"
    }
}