package gambi.zerone.camscanner.helpers

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.util.Matrix
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlin.collections.forEach
import kotlin.io.use
import kotlin.let
import kotlin.ranges.until
import kotlin.run
import kotlin.text.contains
import kotlin.text.isNotBlank

fun Context.getPdfFolderDir(): File {
    return File(filesDir, "pdf_files").also(File::mkdirs)
}

fun File.isProtected(check: (isProtected: Boolean) -> Unit) {
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    val eCheckPassword = CoroutineExceptionHandler { _, th ->
        th.message?.let {
            if (it.contains("password")) {
                check(true)
                scope.cancel()
            } else {
                check(false)
                scope.cancel()
            }
        } ?: run {
            check(false)
            scope.cancel()
        }
    }
    scope.launch(eCheckPassword) {
        PdfRenderer(
            ParcelFileDescriptor.open(
                this@isProtected,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
        check(false)
        scope.cancel()
    }
}

fun checkPasswordPdf(file: File, pass: String, result: (b: Boolean) -> Unit) {
    val exception = CoroutineExceptionHandler { _, throwable ->
        throwable.message?.let {
            Log.d("Namzzz", "invalidPassword mess = $it")
            result(false)
        }
    }
    CoroutineScope(Job() + Dispatchers.Main).launch(exception) {
        val document = PDDocument.load(file, pass)
        document.close()
        result(true)
    }
}

//fun createPassword(
//    filePdf: File,
//    passwordInput: String,
//    complete: (success: Boolean, newPath: String?) -> Unit
//) {
//    FileUtils.checkFolder(Constant.SAVE_PDF_DOCUMENT_LOCK)
//    val newFile = File(Constant.SAVE_PDF_DOCUMENT_LOCK, filePdf.name)
////    val newPath = "${Constant.SAVE_PDF_DOCUMENT}/${filePdf.name}"
//    val exception = CoroutineExceptionHandler { _, throwable ->
//        throwable.message?.let {
//            if (it.isNotBlank()) {
//                complete(false, null)
//            }
//        }
//    }
//    CoroutineScope(Job() + Dispatchers.IO).launch(exception) {
//        async {
//            val document = PDDocument.load(filePdf)
//            val ap = AccessPermission()
//            ap.setCanPrint(false)
//            val spp = StandardProtectionPolicy(passwordInput, passwordInput, ap)
//            spp.encryptionKeyLength = 128
//            spp.permissions = ap
//            document.protect(spp)
//            document.save(newFile)
//            document.close()
//        }.await()
//        withContext(Dispatchers.Main) {
//            complete(newFile.exists(), newFile.absolutePath)
//        }
//    }
//}
//
//fun removePassword(
//    activity: Activity,
//    fileObject: FileObject,
//    complete: (success: Boolean, newPath: String?) -> Unit
//) {
//    FileUtils.checkFolder(Constant.SAVE_PDF_DOCUMENT_UN_LOCK)
//    val newFile = File(Constant.SAVE_PDF_DOCUMENT_UN_LOCK, fileObject.fileDocument.name)
//    val exception = CoroutineExceptionHandler { _, throwable ->
//        throwable.message?.let {
//            if (it.isNotBlank()) {
//                activity.runOnUiThread {
//                    Log.d("Namzzz", "removePassword $it")
//                    complete(false, null)
//                }
//            }
//        }
//    }
//    CoroutineScope(Job() + Dispatchers.IO).launch(exception) {
//        async {
//            val document = PDDocument.load(fileObject.fileDocument, fileObject.passwordFile)
//            document.isAllSecurityToBeRemoved = true
//            document.save(newFile)
//            document.close()
//        }.await()
//        withContext(Dispatchers.Main) {
//            complete(newFile.exists(), newFile.absolutePath)
//        }
//        cancel()
//    }
//}
//
//fun removePasswordToCache(
//    activity: Activity,
//    fileObject: FileObject,
//    complete: (success: Boolean, cachePath: String) -> Unit
//) {
//    val folderCache = File(activity.cacheDir, Constant.SAVE_PDF_PREVIEW)
//    FileUtils.checkFolder(folderCache)
//    val newFile = File(folderCache, fileObject.fileDocument.name)
//    val exception = CoroutineExceptionHandler { _, throwable ->
//        throwable.message?.let {
//            if (it.isNotBlank()) {
//                activity.runOnUiThread {
//                    Log.d("Namzzz", "removePassword $it")
//                    complete(false, Constant.SAVE_PDF_PREVIEW)
//                }
//            }
//        }
//    }
//    CoroutineScope(Job() + Dispatchers.IO).launch(exception) {
//        async {
//            val document = PDDocument.load(fileObject.fileDocument, fileObject.passwordFile)
//            document.isAllSecurityToBeRemoved = true
//            document.save(newFile)
//            document.close()
//        }.await()
//        withContext(Dispatchers.Main) {
//            complete(newFile.exists(), newFile.absolutePath)
//        }
//    }
//}

fun readFirstPagePdf(file: File): Pair<Int, Bitmap?> {
    val document = PDDocument.load(file)
    val totalPage = document.numberOfPages
    val renderer = PDFRenderer(document)
    val bitmap = renderer.renderImage(0, 1f, ImageType.RGB)
    document.close()
    return Pair(totalPage, bitmap)
}

fun getPdfPageSize(filePath: String, pageIndex: Int): Pair<Float, Float> {
    val document = PDDocument.load(File(filePath))
    val page = document.getPage(pageIndex)
    val mediaBox = page.mediaBox
    val width = mediaBox.width
    val height = mediaBox.height
    document.close()
    return Pair(width, height)
}


/**
 * Using this function of the PDFBox library will cause exception:
 * "Could not find referenced cmap stream Identity-H"
 * when rendering some PDF files generated from text documents.
 * The author still hasn't been able to fix it.
 */
fun File.readPageToBitmap(password: String = "", page: Int, scale: Float = 1f): Bitmap? {
    val bitmap: Bitmap?
    val document = PDDocument.load(this, password)
    val totalPage = document.pages
    if (page in 0 until totalPage.count) {
        val renderer = PDFRenderer(document)
        bitmap = renderer.renderImage(page, scale, ImageType.RGB)
    } else {
        bitmap = null
    }
    document.close()
    return bitmap
}


/**
 * Render image by android.graphics.pdf
 */
fun File.readPageToBitmap(page: Int, scale: Float = 1f): Bitmap? {
    val pdfRender = PdfRenderer(
        ParcelFileDescriptor.open(
            this,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    )
    if (page in 0 until pdfRender.pageCount) {
        val openPage = pdfRender.openPage(page)
        val bitmap =
	        createBitmap((openPage.width * scale).toInt(), (openPage.height * scale).toInt())
        openPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        val newBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0F, 0F, null)
        bitmap.recycle()
        openPage.close()
        return newBitmap
    } else return null
}

//fun mergePdf(
//    fileName: String,
//    listFile: MutableList<FileObject>,
//    complete: (success: Boolean, resultPath: String) -> Unit
//) {
//    FileUtils.checkFolder(Constant.SAVE_PDF_DOCUMENT)
//    val fileSave = File(Constant.SAVE_PDF_DOCUMENT, fileName)
//
//    val merger = PDFMergerUtility()
//    merger.destinationFileName = fileSave.absolutePath
//    val documents = mutableListOf<PDDocument>()
//    for (fileObject in listFile) {
//        Log.d(
//            "Namzzz",
//            "File ${fileObject.fileDocument.absolutePath} has pass: ${fileObject.passwordFile}"
//        )
//        if (fileObject.isProtected) {
//            complete(false, Constant.SAVE_PDF_DOCUMENT)
//            return
//        } else {
//            val document = PDDocument.load(fileObject.fileDocument, fileObject.passwordFile)
//            documents.add(document)
//            merger.addSource(fileObject.fileDocument)
//        }
//    }
//    merger.mergeDocuments(getSettingMemory(listFile))
//    for (doc in documents) {
//        doc.close()
//    }
//
//    if (fileSave.exists()) {
//        complete(true, fileSave.absolutePath)
//    } else {
//        complete(false, Constant.SAVE_PDF_PREVIEW)
//    }
//}
//
//private fun getSettingMemory(listFile: MutableList<FileObject>): MemoryUsageSetting? {
//    var totalSize = 0L
//    listFile.forEach { f ->
//        totalSize += f.fileDocument.length()
//    }
//    return when {
//        totalSize < 5 * 1024 * 1024 -> MemoryUsageSetting.setupMainMemoryOnly()
//        totalSize < 20 * 1024 * 1024 -> MemoryUsageSetting.setupMixed(10 * 1024 * 1024)
//        else -> MemoryUsageSetting.setupTempFileOnly()
//    }
//}

fun Context.splitPagePdf(
    fileName: String,
//    fileObject: FileObject,
    fileOriginal: File,
    listIndex: MutableList<Int>,
    complete: (success: Boolean, resultPath: String) -> Unit,
    callback: (progress: String) -> Unit
) {
//    FileUtils.checkFolder(Constant.SAVE_PDF_DOCUMENT)
//    val fileSave = File(Constant.SAVE_PDF_DOCUMENT, fileName)
    val fileSave = File(getPdfFolderDir(), fileName)

    val pdOutput = PDDocument()
    val docInput = PDDocument.load(fileOriginal)
    val pages = docInput.pages
    for (i in listIndex) {
        callback("$i / ${listIndex.size}")
        if (i in 0 until pages.count) {
            val page = pages[i]
            pdOutput.addPage(PDPage(page.cosObject))
        }
    }

    pdOutput.save(fileSave)
    pdOutput.close()
    docInput.close()
    if (fileSave.exists()) {
        complete(true, fileSave.absolutePath)
    } else {
//        complete(false, Constant.SAVE_PDF_DOCUMENT)
        complete(false, getPdfFolderDir().path)
    }
}

/**
 * The watermark can only be overwritten when the PDF file is unlocked.
 */
fun addImageWatermark(inputPdf: File, outputPdf: File, bitmap: Bitmap) {
    PDDocument.load(inputPdf).use { document ->

        for (page in document.pages) {
            val mediaBox: PDRectangle = page.mediaBox
            val pageWidth = mediaBox.width
            val pageHeight = mediaBox.height

            val scaledBitmap = bitmap.scale(pageWidth.toInt(), pageHeight.toInt())
            val pdfImage = LosslessFactory.createFromImage(document, scaledBitmap)

            // Determine if the page has an "upside-down" coordinate system
            val isUpsideDown = pageHeight < 0

            PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true).use { contentStream ->
                contentStream.saveGraphicsState()
                val scaleX = pageWidth / bitmap.width
                val scaleY = abs(pageHeight) / bitmap.height
                if (isUpsideDown) {
                    // Flip the bitmap to match the upside-down coordinate system
                    contentStream.transform(Matrix.getScaleInstance(1f, -1f))
                    contentStream.transform(Matrix.getTranslateInstance(0f, -pageHeight))
                }

                contentStream.transform(Matrix.getScaleInstance(scaleX, scaleY))
                contentStream.drawImage(pdfImage, 0f, 0f)

                contentStream.restoreGraphicsState()
            }
        }

        document.save(outputPdf)
    }
}