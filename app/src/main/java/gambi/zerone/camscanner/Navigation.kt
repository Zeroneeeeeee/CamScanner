package gambi.zerone.camscanner

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import gambi.zerone.camscanner.globals.Constants
import gambi.zerone.camscanner.helpers.getImageFolder
import gambi.zerone.camscanner.helpers.getUnwrappedImageFolder
import gambi.zerone.camscanner.helpers.saveJPEG
import gambi.zerone.camscanner.helpers.tempFolder
import gambi.zerone.camscanner.helpers.toBitmap
import gambi.zerone.camscanner.helpers.unwrap
import gambi.zerone.camscanner.ui.theme.CutoutBottomAppBar
import gambi.zerone.camscanner.view.files.FileScreen
import gambi.zerone.camscanner.view.mergepdf.MergePDFScreen
import gambi.zerone.camscanner.view.scanner.CameraScan
import gambi.zerone.camscanner.view.scanner.CropScanned
import gambi.zerone.camscanner.view.scanner.listScanned.ListScanned
import gambi.zerone.camscanner.view.splitpdf.SplitPDFScreen
import gambi.zerone.camscanner.view.splitpdf.SplitPreview
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
	var selectedItem by remember { mutableIntStateOf(0) }
	val showBottomBar by remember {
		derivedStateOf {
			backStack.lastOrNull() == Screen.Home ||
					backStack.lastOrNull() == Screen.ListPDF
		}
	}

	LaunchedEffect(backStack) {
		snapshotFlow { backStack.lastOrNull() }
			.distinctUntilChanged()
			.map { it ?: Screen.Home }
			.collect { topScreen -> onTopScreen(topScreen) }
	}

	Scaffold(
		bottomBar = {
			if (showBottomBar) {
				CutoutBottomAppBar(modifier = Modifier.navigationBarsPadding()) {
					NavItem(
						icon = R.drawable.ic_home_outlined,
						filledIcon = R.drawable.ic_home_filled,
						title = stringResource(
							R.string.home
						),
						onSelected = {
							selectedItem = 0
							backStack.clear()
							backStack.add(Screen.Home)
						},
						isSelected = selectedItem == 0,
						modifier = Modifier.weight(1f)
					)
					NavItem(
						icon = R.drawable.ic_files_outlined,
						filledIcon = R.drawable.ic_files_filled,
						title = stringResource(
							R.string.files
						),
						onSelected = {
							selectedItem = 1
							backStack.clear()
							backStack.add(Screen.ListPDF)
						},
						isSelected = selectedItem == 1,
						modifier = Modifier.weight(1f)
					)
					Spacer(modifier.width(64.dp))
					NavItem(
						icon = R.drawable.ic_tool_outlined,
						filledIcon = R.drawable.ic_tool_filled,
						title = stringResource(
							R.string.tools
						),
						onSelected = { selectedItem = 2 },
						isSelected = selectedItem == 2,
						modifier = Modifier.weight(1f)
					)
					NavItem(
						icon = R.drawable.ic_setting_outlined,
						filledIcon = R.drawable.ic_setting_filled,
						title = stringResource(
							R.string.settings
						),
						onSelected = { selectedItem = 3 },
						isSelected = selectedItem == 3,
						modifier = Modifier.weight(1f)
					)
				}
			}
		},
		floatingActionButton = {
			if (showBottomBar) {
				FloatingActionButton(
					onClick = { /*TODO*/ },
					containerColor = Color.White,
					shape = CircleShape,
					modifier = Modifier
						.size(64.dp)
						.offset(y = 48.dp)
						.background(Color(0xFFA4ABF4).copy(alpha = 0.15f), CircleShape)
						.padding(8.dp)
				) {
					Icon(
						painter = painterResource(R.drawable.ic_camera_filled),
						contentDescription = "Camera",
						tint = Color.Unspecified
					)
				}
			}
		},
		floatingActionButtonPosition = FabPosition.Center
	) { innerPadding ->
		NavDisplay(
			modifier = modifier
				.fillMaxSize()
				.padding(innerPadding),
			backStack = backStack,
			onBack = {
				backStack.removeLastOrNull()
			},
			entryProvider = entryProvider {
				entry<Screen.Home> {
					HomeScreen(
						modifier = modifier,
						toSmartScan = { backStack.add(Screen.SmartScan) },
						imageToPdf = { backStack.add(Screen.ListPDF) },
						splitPdf = { backStack.add(Screen.SplitPDF) },
						mergePdf = { backStack.add(Screen.MergePDF) }
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
						openListScan = { backStack.add(Screen.ListScanned) },
						onBack = {
							backStack.removeLastOrNull()
							bitmapAndRotation = null
							ocSaveTask?.cancel()
							ocSaveTask = null
						})
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
				entry<Screen.ListPDF> {
					FileScreen()
				}
				entry<Screen.SplitPDF> {
					SplitPDFScreen(
						toPreview = { uri, splitPage ->
							backStack.add(Screen.SplitPreview(uri, splitPage))
						}
					)
				}
				entry<Screen.SplitPreview> { (uri, splitPages) ->
					SplitPreview(uri = uri, splitPage = splitPages)
				}
				entry<Screen.MergePDF> {
					MergePDFScreen()
				}
				entry<Screen.ListScanned> {
					ListScanned(
						modifier = modifier.fillMaxSize(),
						onBack = { backStack.removeLastOrNull() }
					)
				}
			}
		)
	}
}

@Composable
fun NavItem(
	modifier: Modifier = Modifier,
	icon: Int = R.drawable.ic_tool_outlined,
	filledIcon: Int = R.drawable.ic_tool_outlined,
	isSelected: Boolean = false,
	onSelected: () -> Unit = {},
	title: String = "Title"
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.clickable { onSelected() },
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(
			painter = painterResource(if (isSelected) filledIcon else icon),
			contentDescription = "Nav Item",
			tint = if (!isSelected) Color(0xFFC3C3C3) else Color.Unspecified
		)
		Spacer(Modifier.size(8.dp))
		Text(
			text = title,
			color = if (!isSelected) Color(0xFFC3C3C3) else Color(0xFF4E52D9)
		)
	}
}