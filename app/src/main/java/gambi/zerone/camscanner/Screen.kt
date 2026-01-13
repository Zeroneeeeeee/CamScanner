package gambi.zerone.camscanner

import android.graphics.Bitmap
import android.net.Uri

sealed interface Screen {
    data object Home : Screen
    data object SmartScan : Screen
    data class CropScan(val bitmapAndRotation: Pair<Bitmap, Int>) : Screen
    data object ListPDF: Screen
    data object SplitPDF: Screen
    data class SplitPreview(val name: Uri, val splitPage:List<Int>): Screen
    data object MergePDF: Screen
    data object ListScanned: Screen
}