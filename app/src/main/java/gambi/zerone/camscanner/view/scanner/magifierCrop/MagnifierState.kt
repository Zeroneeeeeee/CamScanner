package gambi.zerone.camscanner.view.scanner.magifierCrop

import androidx.compose.ui.geometry.Offset

data class MagnifierState(
	val visible: Boolean = false,
	val position: Offset = Offset.Zero,
	val bitmapPosition: Offset = Offset.Zero
)
