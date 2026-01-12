package gambi.zerone.camscanner.view.scanner

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageCapture
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import common.libs.compose.toast.CToastConfiguration
import common.libs.compose.toast.CToastHost
import common.libs.compose.toast.CToastState
import common.libs.compose.toast.CToastType
import common.libs.compose.toast.LocalCToastConfig
import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.helpers.captureBitmap
import gambi.zerone.camscanner.helpers.getImage
import gambi.zerone.camscanner.helpers.getImageFolder
import gambi.zerone.camscanner.helpers.observeDirectoryChanges
import gambi.zerone.camscanner.ui.theme.LightBackground
import gambi.zerone.camscanner.view.components.Badge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CameraScan(
    modifier: Modifier = Modifier,
    resultScan: (scanned: Pair<Bitmap, Int>) -> Unit = {},
    onBack: () -> Unit,
) {
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    val cToastState = remember { CToastState() }
    val scope = rememberCoroutineScope()
    var capturing by remember { mutableStateOf(false) }

    val imageCaptureConfig = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(defaultResolutionSelector()).build()
    }

    LaunchedEffect(flashMode) {
        val flow = snapshotFlow { flashMode }
        flow.onEach { imageCaptureConfig.flashMode = it }.launchIn(this)
    }

    BackHandler { onBack() }
    CompositionLocalProvider(LocalCToastConfig provides CToastConfiguration()) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            TopScreen(onBack = onBack, flashMode = flashMode, onClickFlash = { flashMode = it })
            CameraPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                imageCaptureConfig = imageCaptureConfig
            )
            BottomScreen(
                capturing = capturing,
                onClickCapture = {
                    if (capturing) return@BottomScreen
                    capturing = true
                    scope.launch(Dispatchers.IO) {
                        try {
                        	val image = imageCaptureConfig.getImage()
                            val bitmap = image.captureBitmap()
                            val rotation = image.imageInfo.rotationDegrees
                            image.close()
                            resultScan(bitmap to rotation)
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                cToastState.setAndShow(
                                    message = e.message?:"Error",
                                    type = CToastType.ERROR
                                )
                            }
                        } finally {
                        	capturing = false
                        }
                    }
                },
                openListScan = {}
            )
        }

        CToastHost(cToastState, Modifier.systemBarsPadding())
    }
}

fun defaultResolutionSelector() = ResolutionSelector.Builder().apply {
    setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
}.build()

@Composable
fun TopScreen(
    onBack: () -> Unit,
    flashMode: Int,
    onClickFlash: (Int) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .height(55.dp)
                .aspectRatio(1f), onClick = onBack
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "back",
                tint = Color.Unspecified
            )
        }
        Spacer(
            modifier = Modifier
                .height(5.dp)
                .weight(1f)
        )
        IconButton(
            modifier = Modifier
                .height(55.dp)
                .aspectRatio(1f), onClick = {
                if (flashMode == ImageCapture.FLASH_MODE_ON) {
                    onClickFlash(ImageCapture.FLASH_MODE_OFF)
                } else {
                    onClickFlash(ImageCapture.FLASH_MODE_ON)
                }
            }) {
            Icon(
                painter = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> painterResource(R.drawable.ic_flash_on)
                    ImageCapture.FLASH_MODE_OFF -> painterResource(R.drawable.ic_flash_off)
                    else -> painterResource(R.drawable.ic_flash_off)
                }, contentDescription = "flash", tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun BottomScreen(
    capturing: Boolean = false,
    onClickCapture: () -> Unit = {},
    openListScan: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(Modifier.size(50.dp))
        CaptureButton(enabled = !capturing, click = onClickCapture)
        ImagesSmallView(click = openListScan)
    }
}

@Preview(showBackground = true)
@Composable
fun CaptureButton(
    enabled: Boolean = true,
    click: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    val clicked by interactionSource.collectIsPressedAsState()

    val delta by animateDpAsState(
        targetValue = if (clicked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = LinearEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = LinearEasing)
    )

    Box(
        modifier = Modifier
            .clickable(
                enabled = enabled,
                onClick = click,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.capture_button),
            contentDescription = "capture",
            tint = Color.Unspecified
        )

        if (!enabled) {
            CircularProgressIndicator(
                Modifier
                    .alpha(1f - alpha)
                    .size(40.dp - delta),
                strokeCap = StrokeCap.Round,
                strokeWidth = 3.dp,
                color = LightBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImagesSmallViewPreview() {
    ImagesSmallView {}
}

@Composable
fun ImagesSmallView(click: () -> Unit) {
    if (LocalInspectionMode.current) {
        Box(modifier = Modifier.size(45.dp))
        return
    }

    val context = LocalContext.current
    val imageFolder = remember { context.getImageFolder() }

    val images by produceState(initialValue = emptyList(), imageFolder) {
        withContext(Dispatchers.IO) {
            observeDirectoryChanges(imageFolder).collect { updatedFiles ->
                value = updatedFiles
            }
        }
    }

    val mostRecentImage = remember(images) {
        images.maxByOrNull(File::lastModified)
    }

//    if (mostRecentImage != null) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(MaterialTheme.colorScheme.outline)
                .clickable(enabled = true, onClick = click)
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = mostRecentImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Badge(modifier = Modifier.align(Alignment.TopEnd), nr = if (mostRecentImage != null)images.size else 0)
        }
//    } else {
//        Box(
//            modifier = Modifier
//                .size(45.dp)
//                .background(MaterialTheme.colorScheme.outline)
//                .clickable(enabled = true, onClick = {})
//        )
//    }
}