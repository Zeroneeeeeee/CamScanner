package gambi.zerone.camscanner.view.scanner

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun Scanner() {
    val context = LocalContext.current
    val activity = context as Activity

    val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(10)
        .setResultFormats(RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()
    val scanner = GmsDocumentScanning.getClient(options)
    val scannerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->

            Log.d("Scanner", "Day la add on result")

            if (result.resultCode == RESULT_OK) {
                val result =
                    GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                result?.pdf?.let { pdf ->
                    val pdfUri = pdf.uri
                    val pageCount = pdf.pageCount
                    val fileSize = context.getFileSize(pdfUri)
                }
            }

        }

    LaunchedEffect(Unit) {
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                Log.d("Scanner", "Day la add on success listener")
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
            }
    }
}