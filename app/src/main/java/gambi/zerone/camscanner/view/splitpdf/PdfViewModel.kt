package gambi.zerone.camscanner.view.splitpdf

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel

class PdfViewModel : ViewModel() {

    private val _bitmapCache = mutableStateMapOf<Int, Bitmap>()
    val bitmapCache: Map<Int, Bitmap> = _bitmapCache

    suspend fun loadBitmapIfNeeded(
        pageIndex: Int,
        maxWidth: Int = 360,
        maxHeight: Int = 640,
        converter: PdfBitmapConverter
    ) {
        if (_bitmapCache.containsKey(pageIndex)) return

        val bitmap = converter.renderPage(
            pageIndex,
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )

        if (bitmap != null) {
            _bitmapCache[pageIndex] = bitmap
        }
    }

    fun clearCache() {
        _bitmapCache.clear()
    }
}

