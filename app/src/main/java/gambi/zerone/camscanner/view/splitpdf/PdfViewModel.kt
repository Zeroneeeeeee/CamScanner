package gambi.zerone.camscanner.view.splitpdf

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel

class PdfViewModel : ViewModel() {

    private val _bitmapCache = mutableStateMapOf<Int, Bitmap>()
    val bitmapCache: Map<Int, Bitmap> = _bitmapCache

    suspend fun loadBitmapIfNeeded(
        pageIndex: Int,
        converter: PdfBitmapConverter
    ) {
        if (_bitmapCache.containsKey(pageIndex)) return

        val bitmap = converter.renderPage(
            pageIndex,
            maxWidth = 360,
            maxHeight = 640
        )

        if (bitmap != null) {
            _bitmapCache[pageIndex] = bitmap
        }
    }
}

