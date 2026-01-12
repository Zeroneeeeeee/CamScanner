package gambi.zerone.camscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import gambi.zerone.camscanner.ui.theme.CutoutBottomAppBar
import gambi.zerone.camscanner.view.files.FileScreen
import gambi.zerone.camscanner.view.mergepdf.MergePDFScreen
import gambi.zerone.camscanner.view.scanner.CameraScan
import gambi.zerone.camscanner.view.splitpdf.SplitPDFScreen
import gambi.zerone.camscanner.view.splitpdf.SplitPreview

@Composable
fun Navigation(modifier: Modifier = Modifier) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    var selectedItem by remember { mutableIntStateOf(0) }
    val showBottomBar by remember {
        derivedStateOf {
            backStack.lastOrNull() == Screen.Home ||
                    backStack.lastOrNull() == Screen.ListPDF
        }
    }
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CutoutBottomAppBar(modifier = Modifier.navigationBarsPadding()) {
                    NavItem(
                        icon = R.drawable.ic_home_outlined,
                        filledIcon = R.drawable.ic_home_filled,
                        title = stringResource(
                            R.string.home
                        ),
                        onSelected = {
                            selectedItem = 0
                            backStack.clear()
                            backStack.add(Screen.Home)
                        },
                        isSelected = selectedItem == 0,
                        modifier = Modifier.weight(1f)
                    )
                    NavItem(
                        icon = R.drawable.ic_files_outlined,
                        filledIcon = R.drawable.ic_files_filled,
                        title = stringResource(
                            R.string.files
                        ),
                        onSelected = {
                            selectedItem = 1
                            backStack.clear()
                            backStack.add(Screen.ListPDF)
                        },
                        isSelected = selectedItem == 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier.width(64.dp))
                    NavItem(
                        icon = R.drawable.ic_tool_outlined,
                        filledIcon = R.drawable.ic_tool_filled,
                        title = stringResource(
                            R.string.tools
                        ),
                        onSelected = { selectedItem = 2 },
                        isSelected = selectedItem == 2,
                        modifier = Modifier.weight(1f)
                    )
                    NavItem(
                        icon = R.drawable.ic_setting_outlined,
                        filledIcon = R.drawable.ic_setting_filled,
                        title = stringResource(
                            R.string.settings
                        ),
                        onSelected = { selectedItem = 3 },
                        isSelected = selectedItem == 3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { /*TODO*/ },
                    containerColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .offset(y = 48.dp)
                        .background(Color(0xFFA4ABF4).copy(alpha = 0.15f), CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera_filled),
                        contentDescription = "Camera",
                        tint = Color.Unspecified
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavDisplay(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {
                entry<Screen.Home> {
                    HomeScreen(
                        modifier = modifier,
                        toSmartScan = { backStack.add(Screen.SmartScan) },
                        imageToPdf = { backStack.add(Screen.ListPDF) },
                        splitPdf = { backStack.add(Screen.SplitPDF) },
                        mergePdf = { backStack.add(Screen.MergePDF) }
                    )
                }
                entry<Screen.SmartScan> {
                    CameraScan(
                        modifier = modifier,
                        onBack = { backStack.removeLastOrNull() })
                }
                entry<Screen.ListPDF> {
                    FileScreen()
                }
                entry<Screen.SplitPDF> {
                    SplitPDFScreen(
                        toPreview = { uri, splitPage ->
                            backStack.add(Screen.SplitPreview(uri, splitPage))
                        }
                    )
                }
                entry<Screen.SplitPreview> { (uri, splitPages) ->
                    SplitPreview(uri = uri, splitPage = splitPages)
                }
                entry<Screen.MergePDF> {
                    MergePDFScreen()
                }

            }
        )
    }
}

@Composable
fun NavItem(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.ic_tool_outlined,
    filledIcon: Int = R.drawable.ic_tool_outlined,
    isSelected: Boolean = false,
    onSelected: () -> Unit = {},
    title: String = "Title"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable { onSelected() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(if (isSelected) filledIcon else icon),
            contentDescription = "Nav Item",
            tint = if (!isSelected) Color(0xFFC3C3C3) else Color.Unspecified
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = title,
            color = if (!isSelected) Color(0xFFC3C3C3) else Color(0xFF4E52D9)
        )
    }
}