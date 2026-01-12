package gambi.zerone.camscanner.view.scanner.magifierCrop

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.apply
import kotlin.math.roundToInt
import kotlin.ranges.coerceAtMost
import kotlin.ranges.coerceIn

@Composable
fun Magnifier(
	state: MagnifierState,
	bitmap: Bitmap,
	rotation: Int,
	modifier: Modifier = Modifier
) {

	val magnifierSize = 120.dp
	val zoomFactor = 1.5f
	val crosshairColor = Color.Red
	val crosshairStroke = 1.dp
	val rotatedBitmap = remember(bitmap, rotation) {
		if (rotation != 0) {
			val matrix =
				Matrix().apply { postRotate(rotation.toFloat()) }
			Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
		} else {
			bitmap
		}
	}
	val imageBitmap = remember(rotatedBitmap) { rotatedBitmap.asImageBitmap() }
	if (state.visible) {
		Box(
			modifier = modifier
				.offset { IntOffset(30, 30) }
				.size(magnifierSize)
				.drawWithCache {
					val sourceSizePx = (magnifierSize.toPx() / zoomFactor)
					val srcTopLeft = IntOffset(
						x = (state.bitmapPosition.x - sourceSizePx / 2).roundToInt()
							.coerceIn(0, imageBitmap.width),
						y = (state.bitmapPosition.y - sourceSizePx / 2).roundToInt()
							.coerceIn(0, imageBitmap.height)
					)
					val srcSize = IntSize(
						width = (sourceSizePx).roundToInt()
							.coerceAtMost(imageBitmap.width - srcTopLeft.x),
						height = (sourceSizePx).roundToInt()
							.coerceAtMost(imageBitmap.height - srcTopLeft.y)
					)
					val dstSize = IntSize(
						width = size.width.roundToInt(),
						height = size.height.roundToInt()
					)

					onDrawWithContent {
						drawCircle(Color.White)
						drawCircle(
							Color.Gray,
							style = Stroke(width = 2.dp.toPx())
						)
						clipPath(
							path = Path()
								.apply { addOval(size.toRect()) }) {
							drawImage(
								image = imageBitmap,
								srcOffset = srcTopLeft,
								srcSize = srcSize,
								dstOffset = IntOffset.Zero,
								dstSize = dstSize
							)
						}
						val center = this.center
						drawLine(
							color = crosshairColor,
							start = Offset(center.x, center.y - 10.dp.toPx()),
							end = Offset(center.x, center.y + 10.dp.toPx()),
							strokeWidth = crosshairStroke.toPx()
						)
						drawLine(
							color = crosshairColor,
							start = Offset(center.x - 10.dp.toPx(), center.y),
							end = Offset(center.x + 10.dp.toPx(), center.y),
							strokeWidth = crosshairStroke.toPx()
						)
					}
				}
		)
	}
}