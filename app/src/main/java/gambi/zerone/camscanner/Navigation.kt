package gambi.zerone.camscanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import gambi.zerone.camscanner.view.files.FileScreen
import gambi.zerone.camscanner.view.scanner.Scanner

@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeScreen(
                    toSmartScan = { backStack.add(Screen.SmartScan) },
                    imageToPdf = { backStack.add(Screen.ListPDF) }
                )
            }
            entry<Screen.SmartScan> {
                Scanner()
            }
            entry<Screen.ListPDF> {
                FileScreen()
            }
        }
    )
}