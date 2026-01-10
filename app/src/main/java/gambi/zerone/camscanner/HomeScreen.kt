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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreen(
	modifier: Modifier = Modifier,
	toSmartScan: () -> Unit = {},
	toList: () -> Unit = {}
) {
	MainFunction(
		toSmartScan = toSmartScan,
		toList = toList
	)
}

data class FunctionData(
	val name: String,
	val icon: Int,
	val backgroundColor: Color,
	val onClick: () -> Unit = {}
)

@Composable
fun MainFunction(
	modifier: Modifier = Modifier,
	toSmartScan: () -> Unit = {},
	toList: () -> Unit = {}
) {
	val functions = remember {
		listOf(
			FunctionData("Smart Scan", R.drawable.ic_scan, Color(0xFF4E52D9).copy(alpha = 0.1f), toSmartScan),
			FunctionData("PDF List", R.drawable.ic_image_convert, Color(0xFFFFF7E2), toList),
			FunctionData("Merge PDF", R.drawable.ic_merge, Color(0xFFFCDDEC)) {},
			FunctionData("Split PDF", R.drawable.ic_split, Color(0xFFEBFFE6)) {},
			FunctionData("Sign", R.drawable.ic_sign, Color(0xFFFCDDEC)) {},
			FunctionData("Import Files", R.drawable.ic_import_file, Color(0xFFFFF7E2)) {},
			FunctionData("Import Images", R.drawable.ic_image_convert, Color(0xFFFFF7E2)) {},
			FunctionData("Tools", R.drawable.ic_tool, Color(0xFFFFECE8)) {}
		)
	}
	Column(modifier = modifier
		.fillMaxSize()
		.padding(horizontal = 16.dp)) {
		Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
			Text(text = "Main Function", fontSize = 20.sp)
			Spacer(modifier = Modifier.weight(1f))
			Text(text = "View All", color = Color(0xFF9F9F9F), fontSize = 12.sp)
		}
		Spacer(modifier = Modifier.height(16.dp))
		LazyVerticalGrid(
			columns = GridCells.Fixed(4),
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(15.dp)
		) {
			items(functions) { functionData ->
				FunctionItem(
					functionName = functionData.name,
					icon = functionData.icon,
					iconBackground = functionData.backgroundColor,
					onClick = functionData.onClick
				)
			}
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
				.padding(10.dp)
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
