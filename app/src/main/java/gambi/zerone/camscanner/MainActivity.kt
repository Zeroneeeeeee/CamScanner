package gambi.zerone.camscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import common.libs.compose.extensions.SetNavigationBarContentColor
import common.libs.compose.extensions.SetStatusBarContentColor
import gambi.zerone.camscanner.ui.theme.CamScannerTheme
import gambi.zerone.camscanner.ui.theme.CutoutBottomAppBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            window.SetStatusBarContentColor(MaterialTheme.colorScheme.background)
            window.SetNavigationBarContentColor(MaterialTheme.colorScheme.background)
            Navigation()
        }
    }
}