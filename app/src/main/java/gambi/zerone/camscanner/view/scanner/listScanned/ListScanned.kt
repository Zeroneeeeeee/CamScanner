package gambi.zerone.camscanner.view.scanner.listScanned

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import common.libs.compose.toast.CToastConfiguration
import common.libs.compose.toast.CToastHost
import common.libs.compose.toast.CToastState
import common.libs.compose.toast.CToastType
import common.libs.compose.toast.LocalCToastConfig
import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.app.SharedPrefs
import gambi.zerone.camscanner.helpers.DisableOverscroll
import gambi.zerone.camscanner.helpers.getEffectImageFolder
import gambi.zerone.camscanner.helpers.getImageFolder
import gambi.zerone.camscanner.helpers.getOrCreateEffectImageFile
import gambi.zerone.camscanner.helpers.getPdfFolder
import gambi.zerone.camscanner.helpers.getUnwrappedImageFolder
import gambi.zerone.camscanner.helpers.observeDirectoryChanges
import gambi.zerone.camscanner.helpers.rotate90Degrees
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

@Composable
fun ListScanned(
	modifier: Modifier = Modifier,
	onBack: () -> Unit
) {
	val context = LocalContext.current
	var loading by remember { mutableStateOf(false) }
	val cToastState = remember { CToastState() }
	val scope = rememberCoroutineScope()
	val sharedPrefs = remember { SharedPrefs(context) }
	var wantToDeleteAll by remember { mutableStateOf(false) }

	var changingEffect by remember { mutableStateOf(false) }
	var effectSelected by remember {
		val saved = sharedPrefs.effectSelected
		mutableIntStateOf(saved)
	}
	var changedRotation by remember { mutableIntStateOf(0) }

	val imageFolder = remember {
		context.getImageFolder()
	}
	val unwrappedFolder = remember {
		context.getUnwrappedImageFolder()
	}
	val effectFolder = remember {
		context.getEffectImageFolder()
	}
	val pdfFolder = remember {
		context.getPdfFolder()
	}

	val originalImages by produceState(initialValue = emptyList(), imageFolder) {
		withContext(Dispatchers.IO) {
			observeDirectoryChanges(imageFolder).collect { updatedFiles ->
				value = updatedFiles
			}
		}
	}

	val unwrappedImages by produceState(initialValue = emptyList(), unwrappedFolder) {
		withContext(Dispatchers.IO) {
			observeDirectoryChanges(unwrappedFolder).collect { updatedFiles ->
				value = updatedFiles
			}
		}
	}

	val sortedImages = remember(unwrappedImages) { unwrappedImages.sortedBy(File::getName) }
	val activeImageEffectProcessors = remember { mutableStateMapOf<String, Job>() }

	val pagerState =
		rememberPagerState(/*initialPage = originalImages.size - 1, */pageCount = { sortedImages.size })

	val currentPage = remember(sortedImages, pagerState.currentPage) {
		sortedImages.getOrNull(pagerState.currentPage)
	}

	//View screen

	CompositionLocalProvider(LocalCToastConfig provides CToastConfiguration()) {
		Box(modifier = modifier.fillMaxSize()) {
			Column(
				modifier = Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					IconButton(onClick = onBack) {
						Icon(
							painter = painterResource(R.drawable.ic_back),
							contentDescription = "Back",
							tint = Color.Unspecified
						)
					}
					Spacer(
						Modifier
							.height(10.dp)
							.weight(1f)
					)
					Button(onClick = {

					}) { Text(stringResource(R.string.savepdf_in_folder), fontSize = 13.sp) }
				}

				DisableOverscroll {
					HorizontalPager(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f),
						state = pagerState,
						pageSpacing = 16.dp,
						contentPadding = PaddingValues(15.dp),
					) { page ->
						val imageFile = sortedImages[page]

						Box(
							modifier = Modifier.fillMaxSize()
						) {
							var imageLoading by remember { mutableStateOf(true) }
							var file by remember { mutableStateOf<File?>(null) }

							LaunchedEffect(imageFile, effectSelected, changedRotation) {
								imageLoading = true
								try {
									for (r in 0..3) {
										try {
											val job = async(Dispatchers.Default) {
												file = imageFile.getOrCreateEffectImageFile(
													context,
													effectSelected
												)
											}
											activeImageEffectProcessors[imageFile.name] = job
											job.join()
											break
										} catch (_: FileNotFoundException) {
											if (r == 2) {
												imageFile.delete()
												File(imageFolder, imageFile.name).delete()
												File(effectFolder, imageFile.name).delete()
												scope.launch {
													cToastState.setAndShow(
														message = "Error...",
														type = CToastType.ERROR
													)
												}
												if (unwrappedFolder.listFiles()!!
														.isEmpty()
												) onBack()
											}
											delay(1000)
										}
									}
//									imageLoading = false
								} finally {
									activeImageEffectProcessors.remove(imageFile.name)
								}
							}
							val imageRequest = remember(file, changedRotation) {
								file?.let {
									ImageRequest.Builder(context)
										.data(it)
										.crossfade(true)
										.memoryCacheKey("${it.path}:${it.lastModified()}:${changedRotation}")
										.diskCacheKey("${it.path}:${it.lastModified()}:${changedRotation}")
										.build()
								}
							}
							AsyncImage(
								modifier = Modifier.fillMaxSize(),
								model = imageRequest,
								contentDescription = null,
								contentScale = ContentScale.Fit,
								onSuccess = { imageLoading = false },
								onError = { imageLoading = false }
							)

							if (file == null || imageLoading) Box(
								Modifier.fillMaxSize(),
								contentAlignment = Alignment.Center
							) {
								CircularProgressIndicator()
							}
						}
					}
				}

				Text(
					modifier = Modifier
						.background(MaterialTheme.colorScheme.primary, shape = CircleShape)
						.padding(vertical = 4.dp, horizontal = 10.dp),
					text = "${pagerState.currentPage + 1}/${sortedImages.size}",
					fontSize = 13.sp,
					color = Color.White
				)

				Spacer(Modifier.size(55.dp))
			}

			BottomFunctions(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 20.dp)
					.background(
						color = MaterialTheme.colorScheme.primary,
						shape = RoundedCornerShape(15.dp)
					)
					.align(Alignment.BottomCenter),
				clickChangeEffect = {
					if (!changingEffect) changingEffect = true
				},
				clickRotate = {
					if (currentPage != null && currentPage.exists() && !loading) {
						loading = true
						scope.launch(Dispatchers.Default) {
							activeImageEffectProcessors[currentPage.name]?.join()
							currentPage.rotate90Degrees()
							File(effectFolder, currentPage.name).rotate90Degrees()
							changedRotation++
							loading = false
						}
					}
				},
				clickDelete = {
					val imageDelete = sortedImages.getOrNull(pagerState.currentPage)
					imageDelete?.let { imageDelete ->
						scope.launch {
							loading = true
							withContext(Dispatchers.IO) {
								try {
									val originalFile = File(imageFolder, imageDelete.name)
									val effectFile = File(effectFolder, imageDelete.name)
//									val unwrappedFile = imageDelete

									if (originalFile.exists()) originalFile.delete()
									if (effectFile.exists()) effectFile.delete()
									if (imageDelete.exists()) imageDelete.delete()
//									if (unwrappedFile.exists()) unwrappedFile.delete()

								} catch (e: Exception) {
									cToastState.setAndShow(
										message = "Delete failed: ${e.message}",
										type = CToastType.ERROR
									)
								}
							}
							loading = false
							if (unwrappedFolder.listFiles()?.isEmpty() == true) {
								onBack()
							}
						}
					} ?: run {
						scope.launch {
							cToastState.setAndShow(
								message = "No image selected to delete",
								type = CToastType.WARNING
							)
						}
					}
				},
				clickDeleteAll = { wantToDeleteAll = true })
		}

		if (wantToDeleteAll) {
			AlertDialog(
				text = { Text(stringResource(R.string.delete_all_scans), fontSize = 14.sp) },
				onDismissRequest = { wantToDeleteAll = false },
				confirmButton = {
					Button(
						onClick = {
							originalImages.forEach(File::delete)
							unwrappedImages.forEach(File::delete)
							effectFolder.listFiles()!!.forEach(File::delete)
							onBack()
						}
					) { Text(stringResource(R.string.yes)) }
				},
				dismissButton = {
					Button(onClick = { wantToDeleteAll = false }) {
						Text(stringResource(R.string.no))
					}
				}
			)
		}

		CToastHost(cToastState, Modifier.systemBarsPadding())
	}
}

@Composable
private fun BottomFunctions(
	modifier: Modifier = Modifier,
	clickChangeEffect: () -> Unit = {},
	clickRotate: () -> Unit,
	clickDelete: () -> Unit,
	clickDeleteAll: () -> Unit
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.SpaceAround,
		verticalAlignment = Alignment.CenterVertically
	) {
		IconButton(onClick = clickChangeEffect) {
			Icon(
				modifier = Modifier.size(24.dp),
				painter = painterResource(R.drawable.crop_palette),
				contentDescription = "Change effect",
				tint = Color.White
			)
		}
		IconButton(onClick = clickRotate) {
			Icon(
				modifier = Modifier.size(24.dp),
				painter = painterResource(R.drawable.crop_rotate),
				contentDescription = "Rotate",
				tint = Color.White
			)
		}
		IconButton(onClick = clickDelete) {
			Icon(
				modifier = Modifier.size(24.dp),
				painter = painterResource(R.drawable.crop_delete),
				contentDescription = "Delete",
				tint = Color.White
			)
		}
		IconButton(onClick = clickDeleteAll) {
			Icon(
				modifier = Modifier.size(24.dp),
				painter = painterResource(R.drawable.crop_delete_sweep),
				contentDescription = "Delete all",
				tint = Color.White
			)
		}
	}
}