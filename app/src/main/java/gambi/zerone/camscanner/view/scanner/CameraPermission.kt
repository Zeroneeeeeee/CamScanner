package gambi.zerone.camscanner.view.scanner

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import common.libs.compose.functions.openAppSettings
import gambi.zerone.camscanner.app.PermissionPrefs

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandleCameraPermission(
	onPermissionGranted: () -> Unit,
	content: @Composable (requestPermission: () -> Unit) -> Unit
) {
	val context = LocalContext.current
	val permissionPrefs = remember { PermissionPrefs(context) }
	var showPermissionDialog by rememberSaveable { mutableStateOf(false) }

	val cameraPermissionState = rememberPermissionState(
		Manifest.permission.CAMERA
	) { isGranted ->
		if (isGranted) {
			onPermissionGranted()
		} else {
			permissionPrefs.incrementCameraDeniedCount()
			Log.d("Namzzz", ": HomeScreen denied ${permissionPrefs.cameraDeniedCount}")
		}
		showPermissionDialog = false
	}

	val launcherCamera = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) {
		cameraPermissionState.launchPermissionRequest()
		Log.d("Namzzz", "Returned from App Settings. CAMERA launcher")
	}

	if (showPermissionDialog) {
		val rationaleText: String
		val buttonText: String
		val onClickAction: () -> Unit
		val deniedCount = permissionPrefs.cameraDeniedCount

		if (deniedCount >= 2) {
			rationaleText = "Quyền truy cập camera đã bị từ chối vĩnh viễn. Vui lòng vào Cài đặt của ứng dụng để cấp quyền thủ công."
			buttonText = "Mở Cài đặt"
			onClickAction = {
				context.openAppSettings(launcherCamera)
				showPermissionDialog = false
			}
		} else if (deniedCount == 1) {
			rationaleText = "Để quét tài liệu, ứng dụng cần quyền truy cập vào camera. Nếu từ chối lần nữa, bạn sẽ phải cấp quyền thủ công trong Cài đặt."
			buttonText = "Thử lại"
			onClickAction = { cameraPermissionState.launchPermissionRequest() }
		} else {
			rationaleText = "Tính năng quét tài liệu yêu cầu quyền truy cập camera. Vui lòng cấp quyền để sử dụng."
			buttonText = "Cấp quyền"
			onClickAction = { cameraPermissionState.launchPermissionRequest() }
		}

		PermissionRequestScreen(
			rationaleText = rationaleText,
			buttonText = buttonText,
			dismiss = { showPermissionDialog = false },
			onClick = onClickAction
		)
	}
	content {
		if (cameraPermissionState.status.isGranted) {
			onPermissionGranted()
		} else {
			showPermissionDialog = true
		}
	}
}
@Composable
fun PermissionRequestScreen(
	rationaleText: String,
	buttonText: String,
	dismiss: () -> Unit = {},
	onClick: () -> Unit
) {
	Dialog(onDismissRequest = {}) {
		Column(
			modifier = Modifier
				.padding(16.dp)
				.background(MaterialTheme.colorScheme.background),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Text(text = "Yêu Cầu Quyền Camera", style = MaterialTheme.typography.headlineSmall)
			Spacer(modifier = Modifier.height(16.dp))
			Text(text = rationaleText, textAlign = TextAlign.Center)
			Spacer(modifier = Modifier.height(16.dp))
			Row {
				Button(onClick = dismiss) {
					Text("Cancel")
				}
				Button(onClick = onClick) {
					Text(buttonText)
				}
			}
		}
	}
}