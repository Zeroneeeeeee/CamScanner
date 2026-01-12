package gambi.zerone.camscanner.view.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import gambi.zerone.camscanner.helpers.resizeMax
import gambi.zerone.camscanner.helpers.rotatedHeight
import gambi.zerone.camscanner.helpers.rotatedWidth
import gambi.zerone.camscanner.helpers.yuvToMat
import gambi.zerone.camscanner.functions.ImageAnalyser
import gambi.zerone.camscanner.functions.getPoints
import gambi.zerone.camscanner.helpers.offsetAnim
import gambi.zerone.camscanner.helpers.rotate
import gambi.zerone.camscanner.helpers.timesAssign
import gambi.zerone.camscanner.helpers.toOffset
import gambi.zerone.camscanner.ui.theme.BorderScan
import gambi.zerone.camscanner.ui.theme.CirclePointDiameter
import gambi.zerone.camscanner.ui.theme.CirclePointRadius
import gambi.zerone.camscanner.ui.theme.CirclePointWidth
import org.opencv.core.Mat
import org.opencv.core.Point
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCaptureConfig: ImageCapture,
) {
    var parentSize by remember { mutableStateOf(IntSize(1, 1)) }

    var imageWidth by remember { mutableIntStateOf(1) }
    var imageHeight by remember { mutableIntStateOf(1) }

    var points by remember { mutableStateOf(emptyList<Point>()) }

//	val mScale = remember(parentSize, imageWidth, imageHeight) {
//		min( // dùng cho scaleType của camera view là PreviewView.ScaleType.FIT_CENTER
//			parentSize.height.toFloat() / imageHeight.toFloat(),
//			parentSize.width.toFloat() / imageWidth.toFloat()
//		)
//	}
    val mScale = remember(parentSize, imageWidth, imageHeight) {
        max( // dùng cho scaleType của camera view là PreviewView.ScaleType.FILL_CENTER
            parentSize.height.toFloat() / imageHeight.toFloat(),
            parentSize.width.toFloat() / imageWidth.toFloat()
        )
    }

    val scaledWidth = remember(mScale, imageWidth) { imageWidth * mScale }

    val scaledHeight = remember(mScale, imageHeight) { imageHeight * mScale }


    val verticalOffset =
        remember(scaledHeight, parentSize) { (parentSize.height - scaledHeight) / 2 }
    val horizontalOffset =
        remember(scaledWidth, parentSize) { (parentSize.width - scaledWidth) / 2 }

    Box(
        modifier = modifier
            .onSizeChanged { parentSize = it }) {

        val surfaceProvider = previewView(
            modifier = Modifier.fillMaxSize(),
            builder = {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            })

        ImageAnalyser(
            imageAnalysis = remember {
                ImageAnalysis.Builder().setResolutionSelector(defaultResolutionSelector())
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
            },
            imageCapture = imageCaptureConfig,
            preview = remember {
                val preview: Preview = Preview.Builder()
                    .setResolutionSelector(defaultResolutionSelector())
                    .build()

                preview.surfaceProvider = surfaceProvider
                preview
            },
            analyze = {
                val mat = it.yuvToMat()

                val resized = Mat()
                val scale = mat.resizeMax(resized, 300.0)
                mat.release()
                val foundPoints = getPoints(resized)
                foundPoints.rotate(
                    it.imageInfo.rotationDegrees,
                    Point(resized.width() / 2.0, resized.height() / 2.0)
                )

                resized.release()

                foundPoints *= scale


                imageWidth = it.rotatedWidth()
                imageHeight = it.rotatedHeight()
                points = foundPoints
            })
        QuadrilateralPath(
            points = points,
            horizontalOffset = horizontalOffset,
            verticalOffset = verticalOffset,
            scale = mScale
        )
        repeat(4) {
            SimpleTargetCircle(
                getOffset = { points.getOrNull(it)?.toOffset() },
                horizontalOffset = horizontalOffset,
                verticalOffset = verticalOffset,
                scale = mScale
            )
        }

    }
}

@Composable
fun previewView(
    modifier: Modifier = Modifier,
    builder: PreviewView.() -> Unit = {}
): Preview.SurfaceProvider {
    val context = LocalContext.current
    val view = remember { PreviewView(context) }
    val surfaceProvider = remember { view.surfaceProvider }

    AndroidView(modifier = modifier, factory = {
        view.apply(builder)
    })

    return surfaceProvider
}


@Composable
private fun SimpleTargetCircle(
    getOffset: () -> Offset?,
    horizontalOffset: Float,
    verticalOffset: Float,
    scale: Float,
    circleColor: Color = BorderScan,
) {

    val offset = getOffset() ?: return


    val animatedOffset by animateOffsetAsState(
        targetValue = offset,
        animationSpec = offsetAnim
    )


    Box(
        Modifier
            .offset {
                IntOffset(
                    (horizontalOffset + animatedOffset.x * scale - CirclePointRadius.toPx()).roundToInt(),
                    (verticalOffset + animatedOffset.y * scale - CirclePointRadius.toPx()).roundToInt()
                )
            }

            .background(Color(255, 255, 255, 60), shape = CircleShape)
            .size(CirclePointDiameter)
            .border(CirclePointWidth, circleColor, shape = CircleShape))
}

@Composable
fun QuadrilateralPath(
    points: List<Point>,
    horizontalOffset: Float,
    verticalOffset: Float,
    scale: Float,
    color: Color = BorderScan
) {
    if (points.size != 4) return

    val animatedOffsets = points.map { point ->
        val rawOffset = point.toOffset()
        val animatedState by animateOffsetAsState(
            targetValue = rawOffset,
            animationSpec = offsetAnim
        )
        animatedState
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        fun toScreenCoordinate(rawOffset: Offset): Offset {
            return Offset(
                x = horizontalOffset + rawOffset.x * scale,
                y = verticalOffset + rawOffset.y * scale
            )
        }

        val path = Path()

        val p0 = toScreenCoordinate(animatedOffsets[0])
        path.moveTo(p0.x, p0.y)

        for (i in 1 until 4) {
            val p = toScreenCoordinate(animatedOffsets[i])
            path.lineTo(p.x, p.y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = CirclePointWidth.toPx())
        )

        drawPath(
            path = path,
            color = color.copy(alpha = 0.15f),
            style = Fill
        )
    }
}