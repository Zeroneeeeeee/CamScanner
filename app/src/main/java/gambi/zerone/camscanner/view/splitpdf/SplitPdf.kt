package gambi.zerone.camscanner.view.splitpdf

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gambi.zerone.camscanner.R
import java.io.File
import kotlin.io.copyTo
import kotlin.io.outputStream
import kotlin.use

@Composable
fun SplitPDFScreen(
    modifier: Modifier = Modifier,
    toPreview: (Uri, List<Int>) -> Unit,
    viewModel: PdfViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    val pdfBitmapConverter = remember { PdfBitmapConverter(context) }

    val indexToMove = remember { mutableStateListOf<Int>() }

    val choosePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        pdfUri = it
        it?.let { uri -> pdfBitmapConverter.openPdf(uri) }
    }

    if (pdfUri == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { choosePdfLauncher.launch("application/pdf") }) {
                Text("Choose PDF")
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val pages = pdfBitmapConverter.pageCount
                val chunked = (0 until pages).chunked(2)
                items(chunked.size) { rowIndex ->
                    val rowItems = chunked[rowIndex]

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { pageIndex ->

                            val bitmap = viewModel.bitmapCache[pageIndex]

                            LaunchedEffect(pageIndex) {
                                if (bitmap == null) {
                                    viewModel.loadBitmapIfNeeded(
                                        pageIndex,
                                        pdfBitmapConverter
                                    )
                                }
                            }

                            bitmap?.let {
                                var isAdd by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .clickable(
                                            onClick = {
                                                isAdd = !isAdd
                                                if (isAdd) {
                                                    indexToMove.add(pageIndex)
                                                } else {
                                                    indexToMove.remove(pageIndex)
                                                }
                                            }
                                        )
                                        .weight(1f)
                                        .border(2.dp, if (isAdd) Color.Blue else Color.Transparent),
                                ) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                    )
                                    if (isAdd) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_tick),
                                            contentDescription = null,
                                            tint = Color.Blue,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(24.dp)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }

                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

            }

            Button(onClick = {
                pdfUri?.let { uri -> toPreview(uri, indexToMove.toList()) }
            }) {
                Text("Split PDF")
            }
        }
    }
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

fun Uri.toFile(context: Context): File {
    val fileName = queryFileName(context, this) ?: "temp_file"
    val file = File(context.cacheDir, fileName)

    context.contentResolver.openInputStream(this)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file
}

fun queryFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return uri.lastPathSegment
}
