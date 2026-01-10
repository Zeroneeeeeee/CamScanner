package gambi.zerone.camscanner.view.splitpdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import gambi.zerone.camscanner.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.use

@Composable
fun SplitPDFScreen(
    modifier: Modifier = Modifier,
    fileUri: Uri? = null,
    toPreview: (name: String, List<Bitmap>, List<Bitmap>) -> Unit
) {
    val context = LocalContext.current
    val pdfBitmapConverter = remember {
        PdfBitmapConverter(context)
    }

    val indexToMove = mutableListOf<Int>()

    var pdfUri by remember {
        mutableStateOf(fileUri)
    }
    var renderedPages by remember {
        mutableStateOf<List<Bitmap>>(emptyList())
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var searchResults by remember {
        mutableStateOf(emptyList<SearchResults>())
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(fileUri) {
        pdfUri = fileUri
        pdfUri?.let { uri ->
            renderedPages = pdfBitmapConverter.pdfToBitmaps(uri, ratio = 3)
        }
    }

    val choosePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        pdfUri = it
    }

    if (pdfUri == null) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                choosePdfLauncher.launch("application/pdf")
            }) {
                Text(text = "Choose PDF")
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                item {
                    renderedPages.chunked(2).forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEachIndexed { columnIndex, page ->
                                val index = rowIndex * 2 + columnIndex
                                var isAdd by remember { mutableStateOf(false) }

                                PdfPage(
                                    page = page,
                                    searchResults = searchResults.find { it.page == index },
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            isAdd = !isAdd
                                            if (isAdd) indexToMove.add(index)
                                            else indexToMove.remove(index)
                                        })
                                        .weight(1f)
                                )
                            }

                            // nếu số phần tử lẻ, thêm spacer cho đủ 2 cột
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Button(onClick = {
                val (first, second) = splitPDF(renderedPages, indexToMove)
                toPreview(context.getFileName(pdfUri!!) ?: "Unknown", first, second)
            }) {
                Text(text = "Split PDF")
            }
            if (Build.VERSION.SDK_INT >= 35) {
                OutlinedTextField(
                    value = searchText,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = {
                                searchText = ""
                                searchResults = emptyList()
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    onValueChange = { newSearchText ->
                        searchText = newSearchText

                        pdfBitmapConverter.renderer?.let { renderer ->
                            scope.launch(Dispatchers.Default) {
                                searchResults = (0 until renderer.pageCount).map { index ->
                                    renderer.openPage(index).use { page ->
                                        val results = page.searchText(newSearchText)

                                        val matchedRects = results.map {
                                            it.bounds.first()
                                        }

                                        SearchResults(
                                            page = index,
                                            results = matchedRects
                                        )
                                    }
                                }
                            }
                        }
                    }
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

fun splitPDF(file: List<Bitmap>, moveIndexed: List<Int>): Pair<List<Bitmap>, List<Bitmap>> {
    val first = file.toMutableList()
    val second = moveIndexed.map { file[it] }
    moveIndexed
        .sortedDescending()
        .forEach { first.removeAt(it) }
    return Pair(first, second)
}

fun Context.getFileName(uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)

    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) {
                return cursor.getString(index)
            }
        }
    }
    return null
}