package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/**
 * Screen-agnostic model for standings rows.
 * Map domain/API standings into this model before rendering the card.
 */
data class DivisionStandingUiModel(
    val rank: Int,
    val teamId: Int,
    val teamAbbreviation: String,
    val teamName: String,
    val wins: Int,
    val losses: Int,
    val gamesBack: String,
    val winPct: Float = 0.0F,
    val isSelectedTeam: Boolean = false
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DivisionStandingsCard(
    title: String,
    standings: List<DivisionStandingUiModel>,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    showHeaderAction: Boolean = false,
    headerActionText: String = "View League",
    onHeaderActionClick: (() -> Unit)? = null,
    onTeamClick: ((DivisionStandingUiModel) -> Unit)? = null,
    subtitle: (DivisionStandingUiModel) -> String = { team ->
        "PCT ${String.format(Locale.US, "%.3f", team.winPct).removePrefix("0") }"
    }
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showHeaderAction && onHeaderActionClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onHeaderActionClick) {
                    Text(headerActionText)
                }
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = titleModifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            standings.forEachIndexed { index, team ->
                DivisionStandingRow(
                    team = team,
                    subtitle = subtitle(team),
                    shape = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = standings.size
                    ).shape,
                    onClick = onTeamClick?.let { click ->
                        { click(team) }
                    }
                )
            }
        }
    }
}

/**
 * Backward-compatible wrapper for screens that already use DivisionStandingsSection.
 */
@Composable
fun DivisionStandingsSection(
    title: String,
    standings: List<DivisionStandingUiModel>,
    onViewLeagueClick: () -> Unit,
    onTeamClick: ((DivisionStandingUiModel) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DivisionStandingsCard(
        title = title,
        standings = standings,
        modifier = modifier,
        showHeaderAction = true,
        onHeaderActionClick = onViewLeagueClick,
        onTeamClick = onTeamClick
    )
}

@Composable
private fun DivisionStandingRow(
    team: DivisionStandingUiModel,
    subtitle: String,
    shape: Shape,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val selected = team.isSelectedTeam

    val containerColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        isDark -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainer
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

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.34f else 0.22f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.32f else 0.18f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = containerColor,
        //border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                RankBadge(
                    rank = team.rank.takeIf { it > 0 }?.toString() ?: "-",
                    selected = selected
                )
            }

            Box(
                modifier = Modifier.width(52.dp),
                contentAlignment = Alignment.Center
            ) {
                TeamLogo(
                    teamId = team.teamId,
                    teamName = team.teamName,
                    selected = selected
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = team.teamName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${team.wins}-${team.losses}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )

                Text(
                    text = "GB ${team.gamesBack}",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryContentColor,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TeamLogo(
    teamId: Int,
    teamName: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageUrl = "https://www.mlbstatic.com/team-logos/$teamId.svg"

    val logoBackground = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .background(logoBackground, CircleShape)
            .padding(if (selected) 5.dp else 6.dp),
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

@Composable
private fun RankBadge(
    rank: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val shape: Shape = if (selected) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 10.dp,
            bottomEnd = 18.dp,
            bottomStart = 10.dp
        )
    } else {
        CircleShape
    }

    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(name = "Division Standings Card", showBackground = true)
@Composable
private fun DivisionStandingsCardPreview() {
    MaterialTheme {
        DivisionStandingsCard(
            title = "NL East",
            standings = sampleStandings(),
            onTeamClick = {}
        )
    }
}

private fun sampleStandings(): List<DivisionStandingUiModel> {
    return listOf(
        DivisionStandingUiModel(
            rank = 1,
            teamId = 144,
            teamAbbreviation = "ATL",
            teamName = "Braves",
            wins = 12,
            losses = 5,
            gamesBack = "-",
            winPct = 0.706F
        ),
        DivisionStandingUiModel(
            rank = 2,
            teamId = 143,
            teamAbbreviation = "PHI",
            teamName = "Phillies",
            wins = 10,
            losses = 7,
            gamesBack = "2.0",
            winPct = 0.588F,
            isSelectedTeam = true
        ),
        DivisionStandingUiModel(
            rank = 3,
            teamId = 121,
            teamAbbreviation = "NYM",
            teamName = "Mets",
            wins = 9,
            losses = 8,
            gamesBack = "3.0",
            winPct = 0.529F
        )
    )
}
