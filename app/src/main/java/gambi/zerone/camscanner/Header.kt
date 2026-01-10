package gambi.zerone.camscanner

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Header(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        Icon(
            painter= painterResource(R.drawable.ic_back),
            contentDescription = "Back",
        )
        Text(
            text = stringResource(R.string.select_a_file),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}