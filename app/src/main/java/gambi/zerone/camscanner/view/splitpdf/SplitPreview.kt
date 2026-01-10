package gambi.zerone.camscanner.view.splitpdf

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import gambi.zerone.camscanner.view.splitpdf.PdfPage
import gambi.zerone.camscanner.view.splitpdf.bitmapsToPdf

@Composable
fun SplitPreview(
    modifier: Modifier = Modifier,
    name: String,
    first: List<Bitmap>,
    second: List<Bitmap>
) {
    val context = LocalContext.current

    val fileSafeName = name
        .replace(".pdf", "", ignoreCase = true)
        .replace(" ", "_")
        .lowercase()

    Column(modifier = modifier){
        Preview(title = "First", file = first)
        Spacer(Modifier.height(16.dp))
        Preview(title = "Second", file = second)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            bitmapsToPdf(context, first, "${fileSafeName}(FIRST).pdf")
            bitmapsToPdf(context, second, "${fileSafeName}(SECOND).pdf")
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