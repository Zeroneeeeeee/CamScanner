package gambi.zerone.camscanner.view.mergepdf

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import gambi.zerone.camscanner.view.splitpdf.PdfBitmapConverter
import gambi.zerone.camscanner.view.splitpdf.PdfViewModel
import gambi.zerone.camscanner.view.splitpdf.toFile

@Composable
fun MergePDFScreen(
    modifier: Modifier = Modifier,
    viewModel: PdfViewModel = viewModel()
) {
    val context = LocalContext.current
    val pdfBitmapConverter = remember {
        PdfBitmapConverter(context)
    }
    var pdfUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val merger = PDFMergerUtility()

    LaunchedEffect(pdfUris) {
        pdfUris.forEach { pdfUri ->
            merger.addSource(pdfUri.toFile(context))
        }
    }

    val choosePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) {
        pdfUris = it
        pdfBitmapConverter.openPdfs(pdfUris)
    }

    if (pdfUris.isEmpty()) {
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pages = pdfBitmapConverter.pageCount
                val chunked = (0 until pages).chunked(2)
                items(chunked.size) { rowIndex ->
                    val rowItems = chunked[rowIndex]

                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { pageIndex ->

                            val bitmap = viewModel.bitmapCache[pageIndex]

                            LaunchedEffect(pageIndex) {
                                if (bitmap == null) {
                                    viewModel.loadBitmapIfNeeded(
                                        pageIndex = pageIndex,
                                        converter = pdfBitmapConverter
                                    )
                                }
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                )
                            }

                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Button(onClick = {
                //bitmapsToPdf(context, renderedPages, "Merged_PDF.pdf")
            }) {
                Text(text = "Merge PDF")
            }
        }
    }
}