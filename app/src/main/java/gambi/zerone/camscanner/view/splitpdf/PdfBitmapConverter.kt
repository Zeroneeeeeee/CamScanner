package gambi.zerone.camscanner.view.splitpdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfBitmapConverter(
    private val context: Context,
) {
    var renderer: PdfRenderer? = null

    suspend fun pdfToBitmaps(contentUri: Uri, ratio: Int = 1): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            renderer?.close()

            context
                .contentResolver
                .openFileDescriptor(contentUri, "r")
                ?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        renderer = this

                        return@withContext (0 until pageCount).map { index ->
                            async {
                                openPage(index).use { page ->
                                    val bitmap = createBitmap(
                                        page.width / ratio,
                                        page.height / ratio,
                                        config = Bitmap.Config.ARGB_8888
                                    )

                                    val canvas = Canvas(bitmap).apply {
                                        drawColor(Color.WHITE)
                                        drawBitmap(bitmap, 0f, 0f, null)
                                    }

                                    page.render(
                                        bitmap,
                                        null,
                                        null,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                    )

                                    bitmap
                                }
                            }
                        }.awaitAll()
                    }
                }
            return@withContext emptyList()
        }
    }
}

fun bitmapsToPdf(
    context: Context,
    bitmaps: List<Bitmap>,
    fileName: String = "output.pdf"
): File {
    val pdfDocument = PdfDocument()

    bitmaps.forEachIndexed { index, bitmap ->
        val pageInfo = PdfDocument.PageInfo.Builder(
            bitmap.width,
            bitmap.height,
            index + 1
        ).create()

        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)
    }

    val file = File(context.cacheDir, fileName)
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    return file
}