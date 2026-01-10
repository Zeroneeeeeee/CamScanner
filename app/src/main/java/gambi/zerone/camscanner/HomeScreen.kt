package gambi.zerone.camscanner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//@Preview(showBackground = true)
@Composable
fun HomeScreen(
	modifier: Modifier = Modifier,
	toSmartScan: () -> Unit = {},
	imageToPdf: () -> Unit = {},
	mergePdf: () -> Unit = {},
	splitPdf: () -> Unit = {},
	sign: () -> Unit = {},
	translatePdf: () -> Unit = {},
	openTools: () -> Unit = {},
	clickSearch: () -> Unit = {},
) {
	Column(modifier = modifier.fillMaxSize()) {
		TopHomeScreen(clickSearch = clickSearch)
		MainFunction(
			toSmartScan = toSmartScan,
			imageToPdf = imageToPdf,
			mergePdf = mergePdf,
			splitPdf = splitPdf,
			sign = sign,
			translatePdf = translatePdf,
			openTools = openTools
		)
	}
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
	imageToPdf: () -> Unit = {},
	mergePdf: () -> Unit = {},
	splitPdf: () -> Unit = {},
	sign: () -> Unit = {},
	translatePdf: () -> Unit = {},
	openTools: () -> Unit = {}
) {
	val functions = listOf(
		FunctionData(
			stringResource(R.string.smart_scan),
			R.drawable.ic_scan,
			Color(0xFF4E52D9).copy(alpha = 0.1f),
			toSmartScan
		),
		FunctionData(
			stringResource(R.string.image_to_pdf),
			R.drawable.ic_image_convert,
			Color(0xFFFFCA10).copy(0.1f),
			imageToPdf
		),
		FunctionData(
			stringResource(R.string.merge_pdf),
			R.drawable.ic_merge,
			Color(0xFFF175B5).copy(0.1f),
			mergePdf
		),
		FunctionData(
			stringResource(R.string.split_pdf),
			R.drawable.ic_split,
			Color(0xFF42AD29).copy(0.1f),
			splitPdf
		),
		FunctionData(
			stringResource(R.string.sign),
			R.drawable.ic_sign,
			Color(0xFFF175B5).copy(0.1f),
			sign
		),
		FunctionData(
			stringResource(R.string.translate_pdf),
			R.drawable.ic_translate,
			Color(0xFF2B85FF).copy(0.1f),
			translatePdf
		)
	)
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(26.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = stringResource(R.string.main_function),
				fontSize = 20.sp,
				fontWeight = FontWeight.Medium
			)
			Spacer(modifier = Modifier.weight(1f))
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.clickable(enabled = true, onClick = openTools),
				contentAlignment = Alignment.Center
			) {
				Text(
					modifier = Modifier.padding(horizontal = 6.dp),
					text = stringResource(R.string.view_all),
					color = Color(0xFF9F9F9F),
					fontSize = 12.sp
				)
			}
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
		Spacer(modifier = Modifier.height(4.dp))
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

@Composable
fun TopHomeScreen(
	clickSearch: () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(70.dp)
			.padding(horizontal = 16.dp),
		contentAlignment = Alignment.Center
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(42.dp)
				.border(
					width = 1.dp,
					color = MaterialTheme.colorScheme.primaryContainer,
					shape = RoundedCornerShape(15.dp)
				)
				.clip(shape = RoundedCornerShape(15.dp))
				.clickable(
					enabled = true,
					onClick = clickSearch
				),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(Modifier.width(16.dp))
			Text(
				modifier = Modifier.weight(1f),
				text = stringResource(R.string.search),
				color = MaterialTheme.colorScheme.outline,
				fontSize = 14.sp
			)
			Image(
				modifier = Modifier
					.height(40.dp)
					.aspectRatio(1f)
					.background(
						color = MaterialTheme.colorScheme.primary,
						shape = RoundedCornerShape(13.dp)
					),
				painter = painterResource(R.drawable.ic_search_white),
				contentDescription = "search",
				contentScale = ContentScale.Inside
			)
		}
	}
}
