package gambi.zerone.camscanner

import android.graphics.Bitmap

sealed interface Screen {
	data object Home : Screen
	data object SmartScan : Screen
	data object ListPDF : Screen
	data class CropScan(val bitmapAndRotation: Pair<Bitmap, Int>) : Screen
}