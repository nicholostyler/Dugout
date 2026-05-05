package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.domain.TeamDisplayInfo
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.InningScoreUiModel
import nicholos.tyler.dugout.model.ui.LinescoreUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.model.ui.TeamTotalUiModel
import nicholos.tyler.dugout.ui.theme.DugoutTheme

@Composable
fun GameSnapshotCard(
    model: GameSnapshotCardUiModel,
    modifier: Modifier = Modifier,
    showFooter: Boolean = true,
    onClick: (Int) -> Unit = {}
) {
    val isFinal = model.status.equals("Final", ignoreCase = true)
    val useLinescore = isFinal && model.linescore != null

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(model.gameId) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (useLinescore) 16.dp else 20.dp,
                    vertical = if (useLinescore) 14.dp else 18.dp
                ),
            verticalArrangement = Arrangement.spacedBy(if (useLinescore) 8.dp else 14.dp)
        ) {
            if (useLinescore) {
                // Summary Header: AWAY SCORE FINAL SCORE HOME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Away Team
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = model.leftTeam.name.abbreviation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = model.leftTeam.score,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "FINAL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // Home Team
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = model.rightTeam.score,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = model.rightTeam.name.abbreviation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val headerText = when {
                    model.inningText.isNotBlank() -> model.inningText
                    model.status.equals("In Progress", ignoreCase = true) -> "Live"
                    model.status.equals("Final", ignoreCase = true) -> "Final"
                    model.status.lowercase() in listOf("scheduled", "pre-game", "warmup", "preview") -> {
                        model.startTime.ifBlank { "Pre Game" }
                    }
                    else -> model.status
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (model.inningText.isNotBlank() && model.isTopInning != null) {
                        Icon(
                            imageVector = if (model.isTopInning) {
                                Icons.Default.ArrowUpward
                            } else {
                                Icons.Default.ArrowDownward
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!useLinescore) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SnapshotTeamColumn(
                        team = model.leftTeam.name,
                        score = model.leftTeam.score,
                        emphasize = false,
                        isCompact = isFinal,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    Box(
                        modifier = Modifier.width(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "@",
                            style = if (isFinal) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SnapshotTeamColumn(
                        team = model.rightTeam.name,
                        score = model.rightTeam.score,
                        emphasize = true,
                        isCompact = isFinal,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            } else {
                // Linescore Boxscore for Final games
                LinescoreTable(
                    leftTeamName = model.leftTeam.name.shortName,
                    rightTeamName = model.rightTeam.name.shortName,
                    linescore = model.linescore!!,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }


            val footer = listOfNotNull(
                model.outsText.takeIf { it.isNotBlank() },
                model.countText.takeIf { it.isNotBlank() }
            ).joinToString(" • ")

            if (showFooter && footer.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = if (isFinal) 8.dp else 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = footer,
                        style = if (isFinal) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LinescoreTable(
    leftTeamName: String,
    rightTeamName: String,
    linescore: LinescoreUiModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Team Name Column Header
            Text(
                text = "",
                modifier = Modifier.width(60.dp),
                style = MaterialTheme.typography.labelSmall
            )

            // Inning Headers
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                linescore.innings.forEach { inning ->
                    Text(
                        text = inning.number.toString(),
                        modifier = Modifier.width(18.dp),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            VerticalDivider(modifier = Modifier.height(16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.width(8.dp))

            // R H E Headers
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("R", "H", "E").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.width(20.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Team Rows
        TeamLinescoreRow(leftTeamName, linescore.innings.map { it.leftRuns }, linescore.leftTotal, scrollState)
        TeamLinescoreRow(rightTeamName, linescore.innings.map { it.rightRuns }, linescore.rightTotal, scrollState)
    }
}

@Composable
private fun TeamLinescoreRow(
    teamName: String,
    inningRuns: List<String>,
    totals: TeamTotalUiModel,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = teamName,
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.weight(1f).horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            inningRuns.forEach { runs ->
                Text(
                    text = runs,
                    modifier = Modifier.width(18.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        VerticalDivider(modifier = Modifier.height(16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.width(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = totals.runs.toString(),
                modifier = Modifier.width(20.dp),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = totals.hits.toString(),
                modifier = Modifier.width(20.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = totals.errors.toString(),
                modifier = Modifier.width(20.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun SnapshotTeamColumn(
    team: TeamDisplayInfo,
    score: String,
    emphasize: Boolean,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = if (emphasize) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = team.shortName,
            modifier = Modifier.fillMaxWidth(),
            style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 10.dp))

        Text(
            text = score,
            modifier = Modifier.fillMaxWidth(),
            style = if (isCompact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(showBackground = true, widthDp = 420)
@Composable
private fun ScheduledGameSnapshotCardPreview() {
    DugoutTheme {
        GameSnapshotCard(
            model = GameSnapshotCardUiModel(
                gameId = 2,
                leftTeam = TeamScoreUiModel(
                    name = MlbTeams.get(120),
                    score = "—"
                ),
                rightTeam = TeamScoreUiModel(
                    name = MlbTeams.get(143),
                    score = "—"
                ),
                status = "Scheduled",
                startTime = "7:10 PM"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun FinalExtraInningsGameSnapshotCardPreview() {
    val innings = (1..12).map { 
        InningScoreUiModel(it, if (it == 12) "1" else "0", "0")
    }
    DugoutTheme {
        GameSnapshotCard(
            model = GameSnapshotCardUiModel(
                gameId = 3,
                leftTeam = TeamScoreUiModel(
                    name = MlbTeams.get(120),
                    score = "1"
                ),
                rightTeam = TeamScoreUiModel(
                    name = MlbTeams.get(143),
                    score = "0"
                ),
                status = "Final",
                linescore = LinescoreUiModel(
                    innings = innings,
                    leftTotal = TeamTotalUiModel(1, 8, 0),
                    rightTotal = TeamTotalUiModel(0, 4, 1)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
