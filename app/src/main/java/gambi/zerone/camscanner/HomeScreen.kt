package gambi.zerone.camscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, toSmartScan: () -> Unit = {}, toList: () -> Unit = {}) {
    Content(toSmartScan = toSmartScan, toList = toList)
}

@Composable
fun Content(modifier: Modifier = Modifier, toSmartScan: () -> Unit = {}, toList: () -> Unit = {}){
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        item {
            MainFunction(
                toSmartScan = toSmartScan,
                toList = toList
            )
        }
    }
}

@Composable
fun MainFunction(modifier: Modifier = Modifier, toSmartScan: () -> Unit = {}, toList: () -> Unit = {}){
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Main Function", fontSize = 20.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "View All", color = Color(0xFF9F9F9F), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Smart Scan",
                icon = R.drawable.ic_scan,
                iconBackground = Color(0xFF4E52D9).copy(alpha = 0.1f),
                onClick = toSmartScan,
            )
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "PDF List",
                icon = R.drawable.ic_image_convert,
                iconBackground = Color(0xFFFFF7E2),
                onClick = toList
            )
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Merge PDF",
                icon = R.drawable.ic_merge,
                iconBackground = Color(0xFFFCDDEC),
                onClick = {}
            )
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Split PDF",
                icon = R.drawable.ic_split,
                iconBackground = Color(0xFFEBFFE6),
                onClick = {}
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Sign",
                icon = R.drawable.ic_sign,
                iconBackground = Color(0xFFFCDDEC),
                onClick = {}
            )
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Import Files",
                icon = R.drawable.ic_import_file,
                iconBackground = Color(0xFFFFF7E2),
                onClick = {}
            )
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Import Images",
                icon = R.drawable.ic_image_convert,
                iconBackground = Color(0xFFFFF7E2),
                onClick = {}
            )
            FunctionItem(
                modifier = Modifier.weight(1f),
                functionName = "Tools",
                icon = R.drawable.ic_tool,
                iconBackground = Color(0xFFFFECE8),
                onClick = {}
            )
        }
    }
}

@Composable
fun FunctionItem(
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.ic_launcher_background,
    functionName: String = "Function Name",
    iconTint: Color = Color.Unspecified,
    iconBackground: Color = Color(0xFF4E52D9).copy(alpha = 0.1f),
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "Function Icon",
            tint = iconTint,
            modifier = Modifier
                .background(iconBackground, shape = RoundedCornerShape(15.dp))
                .padding(16.dp)
                .size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = functionName,
            fontSize = 12.sp,
            color = Color(0xFF5D5D5D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
