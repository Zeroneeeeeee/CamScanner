package gambi.zerone.camscanner.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import gambi.zerone.camscanner.entity.ImageBounds
import gambi.zerone.camscanner.entity.ResizedDimensions

fun Context.tempFolder(): File {
	return File(filesDir, "temp_folder").also(File::mkdirs)
}

fun Context.getImageFolder(): File {
	return File(filesDir, "scan_images").also(File::mkdirs)
}

fun Context.getUnwrappedImageFolder(): File {
	return File(filesDir, "scan_unwrapped-images").also(File::mkdirs)
}

fun Context.getPdfFolder(): File {
	return File(filesDir, "pdf_files").also(File::mkdirs)
}

fun Context.getEffectImageFolder(): File {
	return File(filesDir, "scan_effect-images").also(File::mkdirs)
}

fun Context.clearCacheScanned() {
	val tempFolder = tempFolder()
	val originalImageFolder = getImageFolder()
	val unwrappedImageFolder = getUnwrappedImageFolder()
	val effectImageFolder = getEffectImageFolder()

	val ocFiles = originalImageFolder.listFiles()!!
	val uwFiles = unwrappedImageFolder.listFiles()!!
	val efFiles = effectImageFolder.listFiles()!!
	val temp = File(tempFolder, "temp-image.jpg")
	temp.delete()
	if (ocFiles.size != uwFiles.size) {
		ocFiles.forEach(File::delete)
		uwFiles.forEach(File::delete)
		efFiles.forEach(File::delete)

	}
}

fun File.saveJPEG(bitmap: Bitmap, quality: Int = 91, rotation: Int = 0) {
	this.outputStream().use {
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
		it.flush()
	}
	if (rotation != 0) this.writeRotation(rotation)
}

val Bitmap.largestDimension: Int
	get() {
		return if (width > height) width
		else height
	}

fun File.toBitmap(
	wanted: Int,
	from: Int = wanted - 100,
	to: Int = wanted + 100
): Pair<Bitmap, Int> {
	val bounds = this.getImageBounds()
	val rotation = bounds.rotation
	return toScaledBitmap(wanted, from, to, bounds.largest()) to rotation
}

fun File.bitmapRotation(): Int {
	return exifToDegrees(
		ExifInterface(this).getAttributeInt(
			ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
		)
	)
}

fun exifToDegrees(exifOrientation: Int): Int = when (exifOrientation) {
	ExifInterface.ORIENTATION_ROTATE_90 -> 90
	ExifInterface.ORIENTATION_ROTATE_180 -> 180
	ExifInterface.ORIENTATION_ROTATE_270 -> 270
	else -> 0
}

fun File.toScaledBitmap(
	wanted: Int,
	from: Int = wanted - 100,
	to: Int = wanted + 100,
	originalLargestDimension: Int
): Bitmap {
	val bitmapOptions = BitmapFactory.Options().apply {
		inScaled = false
	}

	if (originalLargestDimension <= to) {
		return BitmapFactory.decodeFile(absolutePath, bitmapOptions)
	}

	val divider = findDivider(wanted, from.toDouble(), to.toDouble(), originalLargestDimension)
	bitmapOptions.inSampleSize = divider


	val firstBitmap = BitmapFactory.decodeFile(absolutePath, bitmapOptions)


	if (to > firstBitmap.largestDimension) return firstBitmap
	val newDimensions = calcResizedDimensions(
		firstBitmap.height,
		firstBitmap.width,
		wanted.toDouble()
	)

	val finalBitmap = firstBitmap.scale(
		newDimensions.calculatedWidth.toInt(),
		newDimensions.calculatedHeight.toInt()
	)
	if (firstBitmap !== finalBitmap) {
		firstBitmap.recycle()

	}
	return finalBitmap
}

private fun File.writeRotation(rotation: Int) {
	val exifInterface = ExifInterface(this.absolutePath)

	val newOrientation = when (rotation) {
		0 -> ExifInterface.ORIENTATION_NORMAL
		90 -> ExifInterface.ORIENTATION_ROTATE_90
		180 -> ExifInterface.ORIENTATION_ROTATE_180
		else -> ExifInterface.ORIENTATION_ROTATE_270
	}

	exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, newOrientation.toString())
	exifInterface.saveAttributes()
}

private val IOExecutor = Dispatchers.IO.asExecutor()
private val DefaultExecutor = Dispatchers.Default.asExecutor()
suspend fun ImageCapture.getImage(): ImageProxy {
	return suspendCoroutine {
		takePicture(DefaultExecutor, object : ImageCapture.OnImageCapturedCallback() {
			override fun onCaptureSuccess(imageProxy: ImageProxy) {
				it.resume(imageProxy)
			}

			override fun onError(exception: ImageCaptureException) {
				it.resumeWithException(exception)
			}
		})
	}
}

fun calcResizedDimensions(
	originalHeight: Int,
	originalWidth: Int,
	maxDimension: Double
): ResizedDimensions {
	val calculatedHeight: Double
	val calculatedWidth: Double
	val scale: Double

	if (originalHeight < maxDimension && originalWidth < maxDimension) {

		return ResizedDimensions(originalHeight.toDouble(), originalWidth.toDouble(), 1.0)

	} else if (originalHeight > originalWidth) {
		calculatedHeight = maxDimension
		calculatedWidth = maxDimension * originalWidth / originalHeight
		scale = originalWidth / calculatedWidth

	} else {
		calculatedWidth = maxDimension
		calculatedHeight = maxDimension * originalHeight / originalWidth
		scale = originalHeight / calculatedHeight
	}

	return ResizedDimensions(
		calculatedHeight = calculatedHeight,
		calculatedWidth = calculatedWidth,
		scale = scale
	)
}

fun clampA4(width: Int, height: Int): Pair<Int, Int> {
	val a4width = 1240f
	val a4height = 1754f

	val flip = width > height

	var scaledWidth: Float
	var scaledHeight: Float
	if (!flip) {
		scaledWidth = width.toFloat()
		scaledHeight = height.toFloat()
	} else {
		scaledWidth = height.toFloat()
		scaledHeight = width.toFloat()
	}

	if (scaledWidth > a4width) {
		val dif = scaledWidth / a4width
		scaledHeight /= dif
		scaledWidth /= dif
	}

	if (scaledHeight > a4height) {
		val dif = scaledHeight / a4height
		scaledHeight /= dif
		scaledWidth /= dif
	}
	return if (!flip) Pair(scaledWidth.toInt(), scaledHeight.toInt())
	else Pair(scaledHeight.toInt(), scaledWidth.toInt())
}

suspend fun <T> ListenableFuture<T>.await(): T {
	return suspendCancellableCoroutine<T> {
		it.invokeOnCancellation { this.cancel(true) }
		this@await.addListener({
			it.resume(this@await.get())
		}, IOExecutor)
	}
}

val offsetAnim = tween<Offset>(durationMillis = 220, easing = LinearEasing)

fun Bitmap.getImageBounds(degrees: Int): ImageBounds {
	return calculateImageBounds(degrees, height, width)
}

fun File.getImageBounds(): ImageBounds {
	val degrees = this.bitmapRotation()
	val bitmapOptions = BitmapFactory.Options().apply {
		inJustDecodeBounds = true
		inScaled = true
	}

	BitmapFactory.decodeFile(this.absolutePath, bitmapOptions)

	val imageWidth = bitmapOptions.outWidth
	val imageHeight = bitmapOptions.outHeight

	return calculateImageBounds(degrees, imageHeight, imageWidth)
}

private fun calculateImageBounds(
	degrees: Int, imageHeight: Int, imageWidth: Int
): ImageBounds {
	return if (degrees % 180 == 90) {
		ImageBounds(
			rotatedWidth = imageHeight,
			rotatedHeight = imageWidth,
			originalWidth = imageWidth,
			originalHeight = imageHeight,
			rotation = degrees
		)
	} else ImageBounds(
		rotatedWidth = imageWidth,
		rotatedHeight = imageHeight,
		originalWidth = imageWidth,
		originalHeight = imageHeight,
		rotation = degrees
	)
}

fun clampA4Paper(width: Int, height: Int): Pair<Int, Int> {
	val a4width = 1240f
	val a4height = 1754f

	val flip = width > height

	var scaledWidth: Float
	var scaledHeight: Float
	if (!flip) {
		scaledWidth = width.toFloat()
		scaledHeight = height.toFloat()
	} else {
		scaledWidth = height.toFloat()
		scaledHeight = width.toFloat()
	}

	if (scaledWidth > a4width) {
		val dif = scaledWidth / a4width
		scaledHeight /= dif
		scaledWidth /= dif
	}

	if (scaledHeight > a4height) {
		val dif = scaledHeight / a4height
		scaledHeight /= dif
		scaledWidth /= dif
	}
	return if (!flip) Pair(scaledWidth.toInt(), scaledHeight.toInt())
	else Pair(scaledHeight.toInt(), scaledWidth.toInt())
}

fun findDivider(wanted: Int, from: Double, to: Double, original: Int): Int {
	var divider = findDivider(wanted, original)
	while (true) {
		val current = original / divider.toDouble()
		if (current in from..to) break
		if (current < from) {
			divider--
			break
		}
		divider++
	}
	return divider
}

fun findDivider(wanted: Int, original: Int): Int {
	var divider = 1

	while (true) {
		val nextDivider = divider * 2
		val divided = original / nextDivider
		if (divided < wanted) break

		divider = nextDivider
	}

	return divider
}