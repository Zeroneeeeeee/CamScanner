package gambi.zerone.camscanner.view.splitpdf

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import gambi.zerone.camscanner.view.splitpdf.SearchResults
import java.io.File

@Composable
fun SplitPreview(
    modifier: Modifier = Modifier,
    uri: Uri,
    splitPage:List<Int>
) {
    val context = LocalContext.current
    val file = uri.toFile(context)
    val src = PDDocument.load(file)
    val out = PDDocument()
    val bitmaps = remember { mutableStateListOf<Bitmap>() }

    splitPage.forEach { page->
        out.importPage(src.getPage(page))
    }

    val rendered = PDFRenderer(out)
    val pageCount = out.numberOfPages
    for (i in 0 until pageCount) {
        val bitmap = rendered.renderImage(i)
        bitmaps.add(bitmap)
    }

    Column(modifier = modifier){
        Preview(title = "Preview", file = bitmaps)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val file = File(context.filesDir, "Example(split).pdf")
            Log.d("PDFBox", "Lưu file thành công tại: ${file.absolutePath}")
            out.save(file)
            out.close()
        }) {
            Text("Split")
        }
    }
}


@Composable
fun Preview(modifier: Modifier = Modifier, title: String, file:List<Bitmap>) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title
        )
        Spacer(Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(file) { index, page ->
                PdfPage(
                    page = page,
                    modifier = Modifier.size(200.dp)
                )
            }
        }

    }
}


@Composable
fun PdfPage(
    page: Bitmap,
    modifier: Modifier = Modifier,
    searchResults: SearchResults? = null
) {
    AsyncImage(
        model = page,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(page.width.toFloat() / page.height.toFloat())
            .drawWithContent {
                drawContent()

                val scaleFactorX = size.width / page.width
                val scaleFactorY = size.height / page.height

                searchResults?.results?.forEach { rect ->
                    val adjustedRect = RectF(
                        rect.left * scaleFactorX,
                        rect.top * scaleFactorY,
                        rect.right * scaleFactorX,
                        rect.bottom * scaleFactorY
                    )

                    drawRoundRect(
                        color = Color.Yellow.copy(alpha = 0.5f),
                        topLeft = Offset(
                            x = adjustedRect.left,
                            y = adjustedRect.top
                        ),
                        size = Size(
                            width = adjustedRect.width(),
                            height = adjustedRect.height()
                        ),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                }
            }
    )
}