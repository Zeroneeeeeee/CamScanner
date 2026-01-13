package gambi.zerone.camscanner.view.files

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import common.libs.compose.extensions.isR30Plus
import gambi.zerone.camscanner.FunctionItem
import gambi.zerone.camscanner.R

@Composable
fun FileScreen(modifier: Modifier = Modifier, onItemClick: (Uri) -> Unit = {}) {
    Content(
        onItemClick = onItemClick
    )
}

@Composable
fun Content(
    modifier: Modifier = Modifier,
    onItemClick: (Uri) -> Unit = {},
    onImportFileClick: () -> Unit = {},
    onImportImageClick: () -> Unit = {},
    onCreateFolderClick: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.size(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceEvenly
        ) {
            FunctionItem(
                icon = R.drawable.ic_import_file,
                functionName = "Import Files",
                iconTint = Color(0xFFFFCA10),
                iconBackground = Color(0xFFFFF7E2),
                onClick = onImportFileClick
            )
            FunctionItem(
                icon = R.drawable.ic_import_image,
                functionName = "Import Images",
                iconTint = Color(0xFF2B85FF),
                iconBackground = Color(0xFFEBF3FF),
                onClick = onImportImageClick
            )
            FunctionItem(
                icon = R.drawable.ic_create_folder_outlined,
                functionName = "Create Folder",
                iconTint = Color(0xFF42AD29),
                iconBackground = Color(0xFFEBFFE6),
                onClick = onCreateFolderClick
            )
        }
        Spacer(Modifier.size(32.dp))
        PdfListScreen(onItemClick = onItemClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PdfListScreen(viewModel: FilesViewModel = viewModel(), onItemClick: (Uri) -> Unit = {}) {
    val context = LocalContext.current
    var isPermissionGranted by remember {
        mutableStateOf(checkPermissionAllFile(context))
    }

    val resultLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            isPermissionGranted = checkPermissionAllFile(context)
        }

    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            viewModel.loadPdfFiles()
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (isPermissionGranted) {
            if (viewModel.pdfFiles.isEmpty()) {
                Text("Không tìm thấy file PDF", Modifier.align(Alignment.Center))
            } else {
                LazyColumn {
                    items(viewModel.pdfFiles) { PdfItem(pdf = it, onClick = onItemClick) }
                }
            }
        } else {
            PermissionRequestUI(
                text = "Vui lòng cấp quyền bộ nhớ để tiếp tục",
                onGrantClick = {
                    requestPermissionAllFile(resultLauncher, context)
                }
            )
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

@Preview(showBackground = true)
@Composable
fun PdfItem(
    modifier: Modifier = Modifier,
    pdf: PdfFile = PdfFile(
        "package:com.example.app".toUri(),
        "Sample PDF",
        1024L,
        System.currentTimeMillis()
    ),
    onClick: (Uri) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(pdf.uri) }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FunctionItem(
                icon = R.drawable.ic_pdf,
                iconBackground = Color(0xFFD70000).copy(alpha = 0.1f),
                hideText = true
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = pdf.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${pdf.dateAdded.formatDate()} | ${pdf.dateAdded.formatTime()} | ${pdf.size.formatFileSize()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

fun checkPermissionAllFile(context: Context): Boolean {
    return if (isR30Plus()) {
        Environment.isExternalStorageManager()
    } else {
        ActivityCompat.checkSelfPermission(
            context,
            WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun requestPermissionAllFile(resultLauncher: ActivityResultLauncher<Intent>, context: Context) {
    if (isR30Plus()) {
        val uri = "package:${context.packageName}".toUri()
        var intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, uri)
        if (intent.resolveActivity(context.packageManager) == null) {
            intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
        resultLauncher.launch(intent)
    } else {
        val activity = context as Activity

        val shouldShowRead =
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                READ_EXTERNAL_STORAGE
            )

        val shouldShowWrite =
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                WRITE_EXTERNAL_STORAGE
            )

        if (shouldShowRead || shouldShowWrite) {
            openAppSettings(resultLauncher, context)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                ),
                Constant.CODE_PERMISSION_ALL_FILE
            )
        }
    }
}

fun openAppSettings(
    resultLauncher: ActivityResultLauncher<Intent>,
    context: Context
) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:${context.packageName}".toUri()
    )
    resultLauncher.launch(intent)
}


object Constant {
    const val CODE_PERMISSION_ALL_FILE = 1001
}