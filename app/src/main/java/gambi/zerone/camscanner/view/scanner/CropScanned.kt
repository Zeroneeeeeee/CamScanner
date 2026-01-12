package gambi.zerone.camscanner.view.scanner

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.functions.ShowImage
import gambi.zerone.camscanner.functions.getPoints
import gambi.zerone.camscanner.helpers.calcResizedDimensions
import gambi.zerone.camscanner.helpers.getImageBounds
import gambi.zerone.camscanner.helpers.rotate
import gambi.zerone.camscanner.helpers.rotateReverseCopy
import gambi.zerone.camscanner.helpers.timesAssign
import gambi.zerone.camscanner.helpers.toCvPoint
import gambi.zerone.camscanner.helpers.toOffset
import gambi.zerone.camscanner.ui.theme.BlueViolet
import gambi.zerone.camscanner.ui.theme.BorderScan
import gambi.zerone.camscanner.ui.theme.CirclePointDiameter
import gambi.zerone.camscanner.ui.theme.CirclePointRadius
import gambi.zerone.camscanner.ui.theme.CirclePointWidth
import gambi.zerone.camscanner.ui.theme.DarkGray
import gambi.zerone.camscanner.view.components.LoadingDialog
import gambi.zerone.camscanner.view.scanner.magifierCrop.Magnifier
import gambi.zerone.camscanner.view.scanner.magifierCrop.MagnifierState
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CropScanned(
	modifier: Modifier = Modifier,
	resultScanned: Pair<Bitmap, Int>, onBack: () -> Unit,
	setNewPoints: (List<Point>) -> Unit
) {
	val originalBitmap = resultScanned.first
	val rotation = resultScanned.second
	var loading by remember { mutableStateOf(false) }

	BackHandler { onBack() }

	val imageBounds = remember(originalBitmap) {
		originalBitmap.getImageBounds(rotation)
	}


	val points = remember(originalBitmap) {
		val newDimensions =
			calcResizedDimensions(originalBitmap.height, originalBitmap.width, 500.0)

		val scaledBitmap =
			originalBitmap.scale(
				newDimensions.calculatedWidth.toInt(),
				newDimensions.calculatedHeight.toInt(),
				true
			)

		val scale = originalBitmap.width / scaledBitmap.width.toDouble()

		val scaledMat = Mat()
		Utils.bitmapToMat(scaledBitmap, scaledMat)
		scaledBitmap.recycle()


		val points = getPoints(scaledMat)
		scaledMat.release()
		points *= scale

		points.rotate(
			rotation,
			Point((originalBitmap.width - 1) / 2.0, (originalBitmap.height - 1) / 2.0)
		)
		points
	}

	val offsetPoints = remember {
		mutableStateListOf(*Array(points.size) { points[it].toOffset() })
	}
	var magnifierState by remember { mutableStateOf(MagnifierState()) }

	var parentSize by remember { mutableStateOf(IntSize(1, 1)) }
	val mScale = remember(parentSize, imageBounds) {
		min(
			parentSize.height.toFloat() / imageBounds.rotatedHeight.toFloat(),
			parentSize.width.toFloat() / imageBounds.rotatedWidth.toFloat()
		)
	}
	val scaledWidth = remember(mScale) {
		imageBounds.rotatedWidth * mScale
	}

	val scaledHeight = remember(mScale) {
		imageBounds.rotatedHeight * mScale
	}

	val verticalOffset = remember(scaledHeight) { (parentSize.height - scaledHeight) / 2 }
	val horizontalOffset = remember(scaledWidth) { (parentSize.width - scaledWidth) / 2 }
	val offset = remember(verticalOffset, horizontalOffset) {
		Offset(
			horizontalOffset,
			verticalOffset
		)
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.onSizeChanged { parentSize = it }
	) {
		ShowImage(
			modifier = Modifier.fillMaxSize(),
			originalBitmap,
			rotation = rotation,
			filter = true
		)
		Magnifier(state = magnifierState, bitmap = originalBitmap, rotation = rotation)

		offsetPoints.forEachIndexed { index, offsetPoint ->
			TargetCircle(
				coordinates = offsetPoint,
				setOffset = { offsetPoints[index] = it },
				horizontalOffset = horizontalOffset,
				scaledWidth = scaledWidth,
				verticalOffset = verticalOffset,
				scaledHeight = scaledHeight,
				scale = mScale,
				circleColor = BorderScan,
				onDragStart = { position ->
					magnifierState = magnifierState.copy(visible = true, position = position)
				},
				onDrag = { newPosition, newBitmapPosition ->
					magnifierState = magnifierState.copy(
						position = newPosition,
						bitmapPosition = newBitmapPosition
					)
				},
				onDragEnd = { magnifierState = magnifierState.copy(visible = false) }
			)
		}
		DrawSquareSet(offsetPoints, mScale, offset, points)

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.TopCenter),
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(onClick = onBack) {
				Icon(
					painter = painterResource(R.drawable.ic_back), contentDescription = "backIcon",
					tint = DarkGray
				)
			}
			Spacer(
				Modifier
					.height(5.dp)
					.weight(1f)
			)
			IconButton(onClick = {
				loading = true
				val cPoints = offsetPoints.map { it.toCvPoint() }
				val finalPoints = cPoints.rotateReverseCopy(
					rotation,
					Point(
						(imageBounds.rotatedWidth - 1) / 2.0,
						(imageBounds.rotatedHeight - 1) / 2.0
					)
				)
				setNewPoints(finalPoints)
			}) {
				Icon(
					painter = painterResource(R.drawable.ic_check),
					contentDescription = "Check",
					tint = Color.DarkGray
				)
			}
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp)
				.align(Alignment.BottomCenter),
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier
					.weight(1f)
					.background(BlueViolet, shape = RoundedCornerShape(15.dp))
					.clip(shape = RoundedCornerShape(15.dp))
					.clickable(
						enabled = true,
						onClick = {
							val corners = listOf(
								Offset(0f, 0f),
								Offset(imageBounds.rotatedWidth.toFloat(), 0f), // Top-right
								Offset(
									imageBounds.rotatedWidth.toFloat(),
									imageBounds.rotatedHeight.toFloat()
								), // Bottom-right
								Offset(0f, imageBounds.rotatedHeight.toFloat()) // Bottom-left
							)
							offsetPoints.clear()
							offsetPoints.addAll(corners)
						}
					),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Spacer(
					Modifier
						.fillMaxWidth()
						.height(5.dp)
				)
				Icon(
					painter = painterResource(R.drawable.crop_full_screen),
					contentDescription = "No crop",
					tint = Color.White
				)
				Text("No crop", fontSize = 11.sp, color = Color.White)
			}

			Spacer(
				Modifier
					.height(5.dp)
					.width(10.dp)
			)

			Column(
				modifier = Modifier
					.weight(1f)
					.background(BlueViolet, shape = RoundedCornerShape(15.dp))
					.clip(shape = RoundedCornerShape(15.dp))
					.clickable(enabled = true, onClick = {
						val autoDetectedOffsets = points.map { it.toOffset() }
						offsetPoints.clear()
						offsetPoints.addAll(autoDetectedOffsets)
					}),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Spacer(
					Modifier
						.fillMaxWidth()
						.height(5.dp)
				)
				Icon(
					painter = painterResource(R.drawable.crop_crop),
					contentDescription = "Auto crop",
					tint = Color.White
				)
				Text("Auto crop", fontSize = 11.sp, color = Color.White)
			}
		}
	}
	LoadingDialog(loading = loading)
}

@Composable
private fun TargetCircle(
	coordinates: Offset,
	setOffset: (Offset) -> Unit = {},
	horizontalOffset: Float,
	scaledWidth: Float,
	verticalOffset: Float,
	scaledHeight: Float,
	scale: Float,
	circleColor: Color = BorderScan,

	onDragStart: (Offset) -> Unit = {},
	onDrag: (newPosition: Offset, newBitmapPosition: Offset) -> Unit = { _, _ -> },
	onDragEnd: () -> Unit = {},
) {

	var temp by remember(coordinates) { mutableStateOf(coordinates) }

	Box(
		Modifier
			.offset {
				IntOffset(
					(horizontalOffset + coordinates.x * scale - CirclePointRadius.toPx()).roundToInt(),
					(verticalOffset + coordinates.y * scale - CirclePointRadius.toPx()).roundToInt()
				)
			}

			.background(Color(255, 255, 255, 40), shape = CircleShape)
			.size(CirclePointDiameter)
			.border(CirclePointWidth, circleColor, shape = CircleShape)
			.pointerInput(horizontalOffset, scaledWidth, verticalOffset, scaledHeight, scale) {
				detectDragGestures(
					onDragStart = {
//						temp = coordinates
						val currentViewPosition = Offset(
							horizontalOffset + coordinates.x * scale,
							verticalOffset + coordinates.y * scale
						)
						onDragStart(currentViewPosition)
					}, onDrag = { change, dragAmount ->
						val nextX = (temp.x + dragAmount.x / scale)
						val nextY = (temp.y + dragAmount.y / scale)
						val viewX = nextX * scale + horizontalOffset
						val viewY = nextY * scale + verticalOffset

						temp = Offset(nextX, nextY)
						when {
							viewX < horizontalOffset -> return@detectDragGestures
							viewX > horizontalOffset + scaledWidth -> return@detectDragGestures
							viewY < verticalOffset -> return@detectDragGestures
							viewY > verticalOffset + scaledHeight -> return@detectDragGestures
						}

						// Tính toạ độ trên bitmap tương ứng với vị trí ngón tay (viewX, viewY)
						val fingerBitmapX = (viewX - horizontalOffset) / scale
						val fingerBitmapY = (viewY - verticalOffset) / scale
						val fingerBitmapPosition = Offset(fingerBitmapX, fingerBitmapY)

						// Gửi đi vị trí ngón tay trên view và vị trí ngón tay trên bitmap
						onDrag(Offset(viewX, viewY), fingerBitmapPosition)

						setOffset(Offset(nextX, nextY))
					}, onDragEnd = onDragEnd, onDragCancel = onDragEnd
				)
			})
}

@Composable
private fun DrawSquareSet(
	offsetPoints: SnapshotStateList<Offset>,
	mScale: Float,
	offset: Offset,
	points: List<Point>,
	color: Color = BorderScan
) {
	Canvas(
		modifier = Modifier.fillMaxSize()
	) {
		val path = Path().apply {
			moveTo(
				offsetPoints[0].x * mScale + offset.x, offsetPoints[0].y * mScale + offset.y
			)
			offsetPoints.forEach {
				lineTo(it.x * mScale + offset.x, it.y * mScale + offset.y)
			}
			close()
		}

		drawPath(
			path = path, color = color.copy(alpha = 0.2f)
		)

		for (i in offsetPoints.indices) {
			val currentOffset = offsetPoints[i]
			val nextOffset = offsetPoints[(i + 1) % points.size]

			drawLine(
				color = color,
				strokeWidth = Stroke.DefaultMiter * 2,
				cap = StrokeCap.Round,
				start = currentOffset * mScale + offset,
				end = nextOffset * mScale + offset
			)

		}
	}
}