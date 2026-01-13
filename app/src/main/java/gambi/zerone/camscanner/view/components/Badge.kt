package gambi.zerone.camscanner.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.ui.theme.DarkGray

@Composable
fun Badge(nr: Int, modifier: Modifier = Modifier) {
	val str = if (nr > 99) "+99"
	else nr.toString()
	BadgedText(str, modifier)
}


@Preview
@Composable
fun BadgePreview() {
	Badge(nr = 5)
}

@Composable
fun BadgedText(text: String, modifier: Modifier = Modifier) {
	Text(
		modifier = modifier
			.background(color = Color.Red, shape = RoundedCornerShape(6.dp))
			.border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(6.dp))
			.padding(horizontal = 3.dp),
		textAlign = TextAlign.Center,
		color = Color.White,
		text = text,
		fontSize = 11.sp
	)
}

@Composable
fun DeleteButton(
	modifier: Modifier = Modifier,
	clickFunction: () -> Unit
) {
	Box(
		modifier = modifier
			.padding(top = 30.dp)
			.size(30.dp)
			.background(color = DarkGray, shape = CircleShape)
			.clickable(onClick = clickFunction)
	) {
		Icon(
			modifier = Modifier
				.align(Alignment.Center)
				.size(22.dp),
			painter = painterResource(R.drawable.crop_delete),
			contentDescription = "Delete",
			tint = Color.White
		)
	}
}

@Preview
@Composable
private fun ClosePreview() {
	DeleteButton {}
}
