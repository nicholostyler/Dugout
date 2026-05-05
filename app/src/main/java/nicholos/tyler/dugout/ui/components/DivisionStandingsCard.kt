package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class DivisionStandingUiModel(
    val rank: Int,
    val teamId: Int,
    val teamAbbreviation: String,
    val teamName: String,
    val wins: Int,
    val losses: Int,
    val gamesBack: String,
    val isSelectedTeam: Boolean = false
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DivisionStandingsSection(
    title: String,
    standings: List<DivisionStandingUiModel>,
    onViewLeagueClick: () -> Unit,
    onTeamClick: ((DivisionStandingUiModel) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),

            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onViewLeagueClick) {
                Text("View League")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            standings.forEachIndexed { index, team ->
                StandingsRow(
                    team = team,
                    shape = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = standings.size
                    ),
                    onClick = if (onTeamClick != null) {
                        { onTeamClick(team) }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StandingsRow(
    team: DivisionStandingUiModel,
    shape: ListItemShapes,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val selected = team.isSelectedTeam

    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val secondaryContentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = shape.shape,
        color = containerColor,
        tonalElevation = if (selected) 3.dp else 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = if (selected) 16.dp else 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankBadge(
                rank = teamRankText(team, fallback = "–"),
                selected = selected
            )

//            Spacer(modifier = Modifier.size(12.dp))
//
//            TeamLogoPlaceholder(
//                abbreviation = team.teamAbbreviation,
//                selected = selected
//            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = team.teamName,
                    style = if (selected) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )

                Text(
                    text = team.teamAbbreviation,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryContentColor
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${team.wins}-${team.losses}",
                    style = if (selected) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )

                Text(
                    text = "GB ${team.gamesBack}",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryContentColor
                )
            }
        }
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
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(if (selected) 34.dp else 28.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamLogoPlaceholder(
    abbreviation: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val shape: Shape = if (selected) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 12.dp,
            bottomEnd = 20.dp,
            bottomStart = 12.dp
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
            .size(if (selected) 42.dp else 36.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = abbreviation.take(3),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Replace this with your real rank from standings data.
 */
private fun teamRankText(
    team: DivisionStandingUiModel,
    fallback: String
): String {
    return team.rank.takeIf { it > 0 }?.toString() ?: fallback
}

@Preview(
    name = "Division Standings - Light",
    showBackground = true
)
@Composable
private fun DivisionStandingsPreview() {
    MaterialTheme {
        DivisionStandingsSection(
            title = "NL East Standings",
            standings = sampleStandings(),
            onViewLeagueClick = {},
            onTeamClick = {}
        )
    }
}

@Preview(
    name = "Division Standings - Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DivisionStandingsDarkPreview() {
    MaterialTheme {
        DivisionStandingsSection(
            title = "NL East Standings",
            standings = sampleStandings(),
            onViewLeagueClick = {},
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
            isSelectedTeam = false
        ),
        DivisionStandingUiModel(
            rank = 2,
            teamId = 143,
            teamAbbreviation = "PHI",
            teamName = "Phillies",
            wins = 10,
            losses = 7,
            gamesBack = "2.0",
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
            isSelectedTeam = false
        ),
        DivisionStandingUiModel(
            rank = 4,
            teamId = 120,
            teamAbbreviation = "WSH",
            teamName = "Nationals",
            wins = 7,
            losses = 10,
            gamesBack = "5.0",
            isSelectedTeam = false
        ),
        DivisionStandingUiModel(
            rank = 5,
            teamId = 146,
            teamAbbreviation = "MIA",
            teamName = "Marlins",
            wins = 6,
            losses = 11,
            gamesBack = "6.0",
            isSelectedTeam = false
        )
    )
}