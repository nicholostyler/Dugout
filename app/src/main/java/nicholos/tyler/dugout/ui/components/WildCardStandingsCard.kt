package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WildCardStandingsCard(
    title: String,
    standings: List<DivisionStandingUiModel>,
    modifier: Modifier = Modifier,
    onTeamClick: (DivisionStandingUiModel) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            standings.forEachIndexed { index, team ->
                // In MLB, top 6 teams are currently "in" the playoffs
                val isPlayoffSpot = index < 6
                
                WildCardStandingRow(
                    team = team,
                    isPlayoffSpot = isPlayoffSpot,
                    rank = index + 1,
                    shape = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = standings.size
                    ).shape,
                    onClick = { onTeamClick(team) }
                )
                
                // Add a visual indicator for the cut line
                if (index == 5 && standings.size > 6) {
                    PlayoffCutLine()
                }
            }
        }
    }
}

@Composable
private fun PlayoffCutLine() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "PLAYOFF CUT LINE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WildCardStandingRow(
    team: DivisionStandingUiModel,
    isPlayoffSpot: Boolean,
    rank: Int,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val selected = team.isSelectedTeam

    val containerColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        isPlayoffSpot -> if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
        else -> if (isDark) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val secondaryContentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        tonalElevation = if (selected) 2.dp else if (isPlayoffSpot) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isPlayoffSpot) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPlayoffSpot) MaterialTheme.colorScheme.primary else contentColor
                )
            }

            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center
            ) {
                TeamLogoSmall(
                    teamId = team.teamId,
                    teamName = team.teamName
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = team.teamName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else if (isPlayoffSpot) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${team.wins}-${team.losses}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.End
                )

                Text(
                    text = String.format(Locale.US, "%.3f", team.winPct).removePrefix("0"),
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryContentColor,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun TeamLogoSmall(
    teamId: Int,
    teamName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl = "https://www.mlbstatic.com/team-logos/$teamId.svg"

    Box(
        modifier = modifier
            .size(32.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = "$teamName logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WildCardStandingsCardPreview() {
    MaterialTheme {
        WildCardStandingsCard(
            title = "American League Wild Card",
            standings = List(15) { i ->
                DivisionStandingUiModel(
                    rank = i + 1,
                    teamId = 143,
                    teamAbbreviation = "PHI",
                    teamName = "Team $i",
                    wins = 90 - i,
                    losses = 60 + i,
                    gamesBack = "0.0",
                    winPct = 0.600f - (i * 0.01f),
                    isSelectedTeam = i == 3
                )
            },
            onTeamClick = {}
        )
    }
}
