package gambi.zerone.camscanner.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingDialog(loading: Boolean) {
	if (loading) {
		Dialog(
			onDismissRequest = { },
			DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.size(100.dp)
					.background(Color.LightGray, shape = RoundedCornerShape(8.dp))
			) {
				CircularProgressIndicator()
			}
		}
	}
}

@Preview
@Composable
private fun LoadingDialogPreview() {
	LoadingDialog(loading = true)
}