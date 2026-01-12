package gambi.zerone.camscanner

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import gambi.zerone.camscanner.globals.Constants
import gambi.zerone.camscanner.helpers.getImageFolder
import gambi.zerone.camscanner.helpers.getUnwrappedImageFolder
import gambi.zerone.camscanner.helpers.saveJPEG
import gambi.zerone.camscanner.helpers.tempFolder
import gambi.zerone.camscanner.helpers.toBitmap
import gambi.zerone.camscanner.helpers.unwrap
import gambi.zerone.camscanner.view.files.FileScreen
import gambi.zerone.camscanner.view.scanner.CameraScan
import gambi.zerone.camscanner.view.scanner.CropScanned
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.util.Date

@Composable
fun Context.Navigation(
	modifier: Modifier = Modifier,
	onTopScreen: (screen: Screen) -> Unit = {}
) {
	var bitmapAndRotation by remember {
		mutableStateOf<Pair<Bitmap, Int>?>(null)
	}
	var ocSaveTask by remember { mutableStateOf<Deferred<File>?>(null) }
	val scope = rememberCoroutineScope()
	val tempFolder = remember { tempFolder() }
	val originalImageFolder = remember { getImageFolder() }
	val unwrappedImageFolder = remember { getUnwrappedImageFolder() }
	val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
	LaunchedEffect(backStack) {
		snapshotFlow { backStack.lastOrNull() }
			.distinctUntilChanged()
			.map { it ?: Screen.Home }
			.collect { topScreen -> onTopScreen(topScreen) }
	}
	NavDisplay(
		backStack = backStack,
		onBack = {
			backStack.removeLastOrNull()
		},
		entryProvider = entryProvider {
			entry<Screen.Home> {
				HomeScreen(
					modifier = modifier,
					toSmartScan = { backStack.add(Screen.SmartScan) },
					imageToPdf = { backStack.add(Screen.ListPDF) }
				)
			}
			entry<Screen.SmartScan> {
				CameraScan(
					modifier = modifier,
					resultScan = {
						bitmapAndRotation = it
						ocSaveTask = scope.async(Dispatchers.IO) {
							val tempFile = File(tempFolder, Constants.TEMP_IMAGE)
							tempFile.saveJPEG(
								bitmap = it.first,
								quality = 100,
								rotation = it.second
							)
							tempFile
						}
						backStack.add(Screen.CropScan(it))
					},
					onBack = {
						backStack.removeLastOrNull()
						bitmapAndRotation = null
						ocSaveTask?.cancel()
						ocSaveTask = null
					})
			}
			entry<Screen.ListPDF> {
				FileScreen()
			}
			entry<Screen.CropScan> { (resultScanned) ->
				CropScanned(
					modifier = modifier.fillMaxSize(),
					resultScanned = resultScanned,
					onBack = { backStack.removeLastOrNull() },
					setNewPoints = { newPoints ->
						if (bitmapAndRotation == null) {
							backStack.removeLastOrNull()
							return@CropScanned
						}
						scope.launch(Dispatchers.IO) {
							val date = Date().time

							val originalMat = Mat()

							Utils.bitmapToMat(
								bitmapAndRotation!!.first,
								originalMat
							)

							val unwrappedMat = unwrap(
								originalMat = originalMat,
								points = newPoints
							)
							originalMat.release()

							val unwrappedBitmap = unwrappedMat.toBitmap()
							unwrappedMat.release()

							val unwrappedFile =
								File(unwrappedImageFolder, "$date.jpg")
							//Lưu ảnh đã được cắt và làm phẳng vào thư mục unwrappedImageFolder
							unwrappedFile.saveJPEG(
								bitmap = unwrappedBitmap,
								rotation = bitmapAndRotation!!.second
							)
							unwrappedBitmap.recycle()

							val originalFile =
								File(originalImageFolder, "$date.jpg")
							//Lưu ảnh gốc vào thư mục originalImageFolder
							val tempFile = ocSaveTask!!.await()
							//Đổi tên file tạm thành file gốc
							tempFile.renameTo(originalFile)

							bitmapAndRotation = null
							withContext(Dispatchers.Main) {
								backStack.removeLastOrNull()
							}
						}
					}
				)
			}
		}
	)
}