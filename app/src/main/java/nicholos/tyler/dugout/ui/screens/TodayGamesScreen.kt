package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TodayGamesScreen(
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        Text(
            text = "Today's Games",
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}