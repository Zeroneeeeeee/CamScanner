package gambi.zerone.camscanner.globals

import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.entity.FilterItem

object Constants {
	const val TEMP_IMAGE = "temp_image.jpg"
	val FilterList = listOf(
		FilterItem("Original", R.mipmap.logo_full, 0),
		FilterItem("Auto Brightness", R.mipmap.logo_full, 1),
		FilterItem("Black & White", R.mipmap.logo_full, 2),
		FilterItem("Document", R.mipmap.logo_full, 3),
		FilterItem("Magic", R.mipmap.logo_full, 4),
		FilterItem("Grayscale", R.mipmap.logo_full, 5),
		FilterItem("Invert", R.mipmap.logo_full, 6),
		FilterItem("Sharpen", R.mipmap.logo_full, 7),
		FilterItem("Warm Tone", R.mipmap.logo_full, 8),
	)
}