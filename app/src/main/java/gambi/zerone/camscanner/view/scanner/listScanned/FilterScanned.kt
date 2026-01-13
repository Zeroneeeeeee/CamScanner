package gambi.zerone.camscanner.view.scanner.listScanned

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gambi.zerone.camscanner.R
import gambi.zerone.camscanner.entity.FilterItem
import gambi.zerone.camscanner.globals.Constants
import gambi.zerone.camscanner.ui.theme.GoldenYellow

@Composable
fun FilterScan(
	modifier: Modifier = Modifier,
	filters: List<FilterItem> = Constants.FilterList,
	filtersSelected: FilterItem = Constants.FilterList[2],
	onFilterClick: (FilterItem) -> Unit,
	accept: () -> Unit
) {
	val lazyListState = rememberLazyListState()
	LaunchedEffect(Unit) {
		val selectedIndex = filters.indexOf(filtersSelected)
		if (selectedIndex > -1) {
			lazyListState.animateScrollToItem(selectedIndex)
		}
	}
	Row(
		modifier = modifier
			.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		LazyRow(
			modifier = Modifier
				.weight(1f)
				.padding(10.dp)
				.background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(15.dp))
				.clip(shape = RoundedCornerShape(15.dp)),
			contentPadding = PaddingValues(7.dp),
			state = lazyListState
		) {
			items(filters) { filterItem ->
				FilterItemView(
					filter = filterItem,
					isSelect = filtersSelected == filterItem,
					onClick = { onFilterClick(filterItem) })
			}
		}
		IconButton(onClick = accept) {
			Icon(
				painter = painterResource(R.drawable.filter_accept),
				tint = Color.Unspecified,
				contentDescription = "Ok"
			)
		}
	}
}

@Composable
fun FilterItemView(
	modifier: Modifier = Modifier,
	filter: FilterItem,
	isSelect: Boolean = false,
	onClick: () -> Unit
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(Modifier.size(4.dp))
		Column(
			modifier = modifier
				.width(72.dp)
				.background(
					if (isSelect) GoldenYellow
					else MaterialTheme.colorScheme.background,
					shape = RoundedCornerShape(8.dp)
				)
				.clip(shape = RoundedCornerShape(8.dp))
				.clickable { onClick() },
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Image(
				painter = painterResource(id = filter.imageResId),
				contentDescription = filter.name,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.size(70.dp)
					.clip(RoundedCornerShape(8.dp))
					.border(
						width = 1.dp,
						color = if (isSelect) GoldenYellow else MaterialTheme.colorScheme.background,
						shape = RoundedCornerShape(8.dp)
					)
			)
			Text(
				modifier = Modifier.padding(horizontal = 2.dp),
				text = filter.name,
				fontSize = 11.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				color = Color.Black
			)
		}
		Spacer(Modifier.size(4.dp))
	}
}