package gambi.zerone.camscanner.entity

import androidx.annotation.DrawableRes

data class FilterItem(
	val name: String,
	@param:DrawableRes val imageResId: Int,
	val mode: Int
)
