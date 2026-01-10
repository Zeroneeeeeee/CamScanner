package gambi.zerone.camscanner.entity

import gambi.zerone.camscanner.helpers.clampA4Paper

data class ImageBounds(
	val rotatedWidth: Int, val rotatedHeight: Int, val originalWidth: Int, val originalHeight: Int, val rotation: Int
) {
	fun largest(): Int {
		return if (originalHeight > originalWidth) originalHeight
		else originalWidth
	}

	fun clampA4(): ImageBounds {
		val (newOriginalWidth, newOriginalHeight) = clampA4Paper(originalWidth, originalHeight)

		val newRotatedWidth: Int
		val newRotatedHeight: Int

		when (rotation) {
			90, 270 -> {
				newRotatedWidth = newOriginalHeight
				newRotatedHeight = newOriginalWidth
			}
			else -> {
				newRotatedWidth = newOriginalWidth
				newRotatedHeight = newOriginalHeight
			}
		}
		return copy(
			rotatedHeight = newRotatedHeight,
			rotatedWidth = newRotatedWidth,
			originalHeight = newOriginalHeight,
			originalWidth = newOriginalWidth
		)
	}
}
