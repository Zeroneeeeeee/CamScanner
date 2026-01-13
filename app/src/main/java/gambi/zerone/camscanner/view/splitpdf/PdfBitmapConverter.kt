package gambi.zerone.camscanner.view.splitpdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfBitmapConverter(
    private val context: Context
) {

    private var renderer: PdfRenderer? = null
    private var descriptor: ParcelFileDescriptor? = null

    private val renderers = mutableListOf<PdfRenderer>()
    private val descriptors = mutableListOf<ParcelFileDescriptor>()
    private val pageOffsets = mutableListOf<Int>()

    private val lock = Any()

    fun openPdf(uri: Uri) {
        openPdfs(listOf(uri))
    }

    fun openPdfs(uris: List<Uri>) {
        close()

        var currentOffset = 0

        uris.forEach { uri ->
            val descriptor =
                context.contentResolver.openFileDescriptor(uri, "r") ?: return@forEach

            val renderer = PdfRenderer(descriptor)

            if (renderers.isEmpty()) {
                this.renderer = renderer
                this.descriptor = descriptor
            }

            renderers.add(renderer)
            descriptors.add(descriptor)
            pageOffsets.add(currentOffset)

            currentOffset += renderer.pageCount
        }
    }

    val pageCount: Int
        get() = if (renderers.isEmpty()) {
            renderer?.pageCount ?: 0
        } else {
            renderers.sumOf { it.pageCount }
        }

    suspend fun renderPage(
        index: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? = withContext(Dispatchers.IO) {

        synchronized(lock) {

            // ===== 1 PDF (backward compatible)
            if (renderers.isEmpty()) {
                val page = renderer?.openPage(index) ?: return@withContext null
                try {
                    renderPageInternal(page, maxWidth, maxHeight)
                } finally {
                    page.close()
                }
            }
            // ===== N PDF
            else {
                val rendererIndex = pageOffsets.indexOfLast { index >= it }
                if (rendererIndex == -1) return@withContext null

                val localIndex = index - pageOffsets[rendererIndex]
                val page = renderers[rendererIndex].openPage(localIndex)

                try {
                    renderPageInternal(page, maxWidth, maxHeight)
                } finally {
                    page.close()
                }
            }
        }
    }

    private fun renderPageInternal(
        page: PdfRenderer.Page,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap {

        val scale = minOf(
            maxWidth.toFloat() / page.width,
            maxHeight.toFloat() / page.height,
            1f
        )

        val bitmapWidth = (page.width * scale).toInt().coerceAtLeast(1)
        val bitmapHeight = (page.height * scale).toInt().coerceAtLeast(1)

        val bitmap = createBitmap(bitmapWidth, bitmapHeight)

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )

        return bitmap
    }

    fun close() {
        renderer?.close()
        renderers.forEach { it.close() }
        renderer = null
        descriptor = null

        renderers.clear()
        descriptors.clear()
        pageOffsets.clear()
    }
}
