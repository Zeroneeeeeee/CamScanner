package gambi.zerone.camscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import common.libs.compose.extensions.SetNavigationBarContentColor
import common.libs.compose.extensions.SetStatusBarContentColor
import gambi.zerone.camscanner.ui.theme.CamScannerTheme
import gambi.zerone.camscanner.ui.theme.LightBackground
import gambi.zerone.camscanner.ui.theme.LightBackground2

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			window.SetStatusBarContentColor(MaterialTheme.colorScheme.background)
			window.SetNavigationBarContentColor(MaterialTheme.colorScheme.background)
			var currentBackgroundColor by remember { mutableStateOf(LightBackground) }
			CamScannerTheme(
				dynamicBackgroundColor = currentBackgroundColor
			) {
				Scaffold { innerPadding ->
					Navigation(
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding),
						onTopScreen = { topScreen ->
							currentBackgroundColor = when (topScreen) {
								is Screen.CropScan -> LightBackground2
								else -> LightBackground
							}
						})
				}
			}
		}
	}
}