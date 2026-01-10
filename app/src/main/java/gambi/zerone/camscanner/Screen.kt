package gambi.zerone.camscanner

sealed interface Screen {
    data object Home : Screen
    data object SmartScan : Screen
    data object ListPDF: Screen

}