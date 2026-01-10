package gambi.zerone.camscanner.view.files

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import gambi.zerone.camscanner.FunctionItem
import gambi.zerone.camscanner.R

@Composable
fun FileScreen(modifier: Modifier = Modifier) {
    Content()
}

@Composable
fun Content(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()){
        Spacer(Modifier.size(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Absolute.SpaceEvenly) {
            FunctionItem(
                functionName = "Import Files"
            )
            FunctionItem(
                functionName = "Import Images"
            )
            FunctionItem(
                functionName = "Create Folder"
            )
        }
        Spacer(Modifier.size(32.dp))
        PdfListScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PdfListScreen(viewModel: FilesViewModel = viewModel()) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_EXTERNAL_STORAGE
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission = permission)

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadPdfFiles()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            permissionState.status.isGranted -> {
                if (viewModel.pdfFiles.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy file PDF nào.")
                    }
                } else {
                    LazyColumn {
                        items(viewModel.pdfFiles) { pdf ->
                            PdfItem(pdf)
                        }
                    }
                }
            }
            permissionState.status.shouldShowRationale -> {
                PermissionRequestUI(
                    text = "Ứng dụng cần quyền truy cập bộ nhớ để hiển thị các file PDF của bạn.",
                    onGrantClick = { permissionState.launchPermissionRequest() }
                )
            }
            else -> {
                PermissionRequestUI(
                    text = "Vui lòng cấp quyền bộ nhớ để tiếp tục.",
                    onGrantClick = { permissionState.launchPermissionRequest() }
                )
            }
        }
    }
}

@Composable
fun PermissionRequestUI(text: String, onGrantClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, modifier = Modifier.padding(16.dp))
        Button(onClick = onGrantClick) {
            Text("Cấp quyền ngay")
        }
    }
}

@Composable
fun PdfItem(pdf: PdfFile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon PDF trực quan hơn
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                tint = Color(0xFFD32F2F), // Màu đỏ đặc trưng của PDF
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = pdf.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: ${pdf.uri}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}