package gambi.zerone.camscanner.view.readfile

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import gambi.zerone.camscanner.FunctionItem
import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.view.splitpdf.PdfBitmapConverter
import gambi.zerone.camscanner.view.splitpdf.PdfViewModel
import kotlinx.coroutines.launch

@Composable
fun ViewPDFScreen(
    modifier: Modifier = Modifier,
    fileUri: Uri,
    onBack: () -> Unit = {},
    viewModel: PdfViewModel = viewModel()
) {
    val context = LocalContext.current
    val pdfBitmapConverter = remember {
        PdfBitmapConverter(context)
    }

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    var pageIndex by remember { mutableStateOf<Int?>(null) }
    var showNavigateDialog by remember { mutableStateOf(false) }

    val currentPage by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo

            if (visibleItems.isEmpty()) return@derivedStateOf 1

            // Lấy item ở giữa màn hình
            val viewportCenter =
                (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            val centerItem = visibleItems.minByOrNull { item ->
                kotlin.math.abs(
                    (item.offset + item.size / 2) - viewportCenter
                )
            }

            (centerItem?.index ?: 0) + 1
        }
    }

    var pageCount by remember { mutableStateOf(0) }

    var pageInput by remember { mutableStateOf("") }

    val enable by remember(pageInput, pageCount) {
        derivedStateOf {
            val page = pageInput.toIntOrNull()
            page != null && page in 1..pageCount
        }
    }


    LaunchedEffect(fileUri) {
        viewModel.clearCache()
        Log.d("Check URI PDF", fileUri.toString())
        pdfBitmapConverter.openPdf(fileUri)
        pageCount = pdfBitmapConverter.pageCount
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3F9))
    ) {
        Column {
            Header(
                onBack = onBack,
                onPageNavigate = {
                    showNavigateDialog = true
                }
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(pageCount) { pageIndex ->
                    val bitmap = viewModel.bitmapCache[pageIndex]

                    LaunchedEffect(pageIndex) {
                        if (bitmap == null) {
                            viewModel.loadBitmapIfNeeded(
                                pageIndex = pageIndex,
                                maxWidth = 1080,
                                maxHeight = 1920,
                                converter = pdfBitmapConverter
                            )
                        }
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .background(Color(0xFFF4F3F9))
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pageCount) { pageIndex ->

                    val bitmap = viewModel.bitmapCache[pageIndex]
                    LaunchedEffect(pageIndex) {
                        if (bitmap == null) {
                            viewModel.loadBitmapIfNeeded(
                                pageIndex = pageIndex,
                                maxWidth = 1080,
                                maxHeight = 1920,
                                converter = pdfBitmapConverter
                            )
                        }
                    }

                    bitmap?.let {
                        Box(
                            modifier = Modifier
                                .border(1.dp, if(currentPage == pageIndex + 1 )Color(0xFF4E52D9) else Color.Transparent, RoundedCornerShape(5.dp))
                                .padding(4.dp)
                                .background(Color.Transparent)
                        ) {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clickable {
                                        scope.launch {
                                            listState.animateScrollToItem(pageIndex)
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 104.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$currentPage / $pageCount",
                color = Color.White
            )
        }
        if (showNavigateDialog) {
            PageNavigateDialog(
                pageInput = pageInput,
                enable = enable,
                onPageChanged = { pageInput = it },
                navigateToPage = {
                    scope.launch {
                        listState.animateScrollToItem(it - 1)
                    }
                },
                onDismiss = {
                    showNavigateDialog = false
                }
            )

        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRotate: () -> Unit = {},
    onPageNavigate: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back),
            contentDescription = "Back",
            modifier = Modifier.clickable {
                onBack()
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.ic_rotation),
            contentDescription = "Rotation",
            modifier = Modifier.clickable {
                onRotate()
            }
        )
        Spacer(modifier = Modifier.size(16.dp))
        Icon(
            painter = painterResource(R.drawable.ic_page_navigate),
            contentDescription = "Page navigate",
            modifier = Modifier.clickable {
                onPageNavigate()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PageNavigateDialog(
    pageInput: String = "123",
    enable: Boolean = true,
    onPageChanged: (String) -> Unit = {},
    navigateToPage: (Int) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                FunctionItem(
                    icon = R.drawable.ic_files_outlined,
                    iconTint = Color(0xFFF175B5),
                    iconBackground = Color(0xFFFCDDEC),
                    hideText = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.go_to_page),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Close",
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.go_to_page), fontSize = 12.sp)
            BasicTextField(
                value = pageInput,
                onValueChange = {
                    onPageChanged(it.filter { ch -> ch.isDigit() })
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onTextLayout = {},
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF4E52D9),
                    lineHeight = 16.sp   // 👈 QUAN TRỌNG
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(
                            color = Color(0xFF4E52D9),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    }
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFCDDEC)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = Color(0xFFF175B5)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Button(
                    enabled = enable,
                    onClick = {
                        pageInput.toIntOrNull()?.let {
                            navigateToPage(it)
                        }
                        onDismiss()
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF175B5)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.go_to_page))
                }
            }
        }
    }
}