package gambi.zerone.camscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import common.libs.compose.extensions.SetNavigationBarContentColor
import common.libs.compose.extensions.SetStatusBarContentColor
import gambi.zerone.camscanner.ui.theme.CamScannerTheme
import gambi.zerone.camscanner.ui.theme.LightBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(this)
        enableEdgeToEdge()
        setContent {
            window.SetStatusBarContentColor(MaterialTheme.colorScheme.background)
            window.SetNavigationBarContentColor(MaterialTheme.colorScheme.background)
            var currentBackgroundColor by remember { mutableStateOf(LightBackground) }
            CamScannerTheme(
                dynamicBackgroundColor = currentBackgroundColor
            ) {
                Navigation()
            }
        }
    }
}