package gambi.zerone.camscanner.helpers

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

@Composable
fun rememberCameraProvider(): ListenableFuture<ProcessCameraProvider> {
	val context = LocalContext.current
	return remember { ProcessCameraProvider.getInstance(context) }
}

/**
 * Một Composable Flow để theo dõi các thay đổi trong một thư mục.
 * Nó phát ra danh sách các tệp mới bất cứ khi nào số lượng tệp thay đổi.
 *
 * @param directory Thư mục cần theo dõi.
 * @param pollingInterval Khoảng thời gian giữa các lần kiểm tra thay đổi.
 * @return Một Flow phát ra danh sách các tệp trong thư mục.
 */
fun observeDirectoryChanges(
	directory: File,
	pollingInterval: Long = 1000L
): Flow<List<File>> = flow {
	var lastFileCount = -1
	while (true) {
		val files = directory.listFiles()?.toList() ?: emptyList()
		if (files.size != lastFileCount) {
			lastFileCount = files.size
			emit(files)
		}
		delay(pollingInterval)
	}
}

@Composable
fun DisableOverscroll(content: @Composable () -> Unit) {
	CompositionLocalProvider(LocalOverscrollFactory provides null, content)
}