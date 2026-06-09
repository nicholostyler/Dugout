package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder

@Composable
fun TeamLogo(
    teamId: Int?,
    teamName: String,
    modifier: Modifier = Modifier
) {
    if (teamId != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://www.mlbstatic.com/team-logos/$teamId.svg")
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = "$teamName logo",
            modifier = modifier.size(32.dp)
        )
    } else {
        Surface(
            modifier = modifier.size(30.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = teamName.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
