package nicholos.tyler.dugout.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.domain.TeamDisplayInfo
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.theme.DugoutTheme

@Composable
fun GameSnapshotCard(
    model: GameSnapshotCardUiModel,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(model.gameId) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val headerText = when {
                model.inningText.isNotBlank() -> model.inningText
                model.status == "In Progress" -> "Live"
                model.status == "Final" -> "Final"
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SnapshotTeamColumn(
                    team = model.leftTeam.name,
                    score = model.leftTeam.score,
                    emphasize = false,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "@",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                SnapshotTeamColumn(
                    team = model.rightTeam.name,
                    score = model.rightTeam.score,
                    emphasize = true,
                    modifier = Modifier.weight(1f)
                )
            }

            val footer = listOfNotNull(
                model.outsText.takeIf { it.isNotBlank() },
                model.countText.takeIf { it.isNotBlank() }
            ).joinToString(" • ")

            if (footer.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = footer,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotTeamColumn(
    team: TeamDisplayInfo,
    score: String,
    emphasize: Boolean,
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = score,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun GameSnapshotCardPreview() {
    DugoutTheme {
        GameSnapshotCard(
            model = GameSnapshotCardUiModel(
                gameId = 1,
                leftTeam = TeamScoreUiModel(
                    name = MlbTeams.get(120),
                    score = "7"
                ),
                rightTeam = TeamScoreUiModel(
                    name = MlbTeams.get(143),
                    score = "2"
                ),
                status = "In Progress",
                inningText = "Bot 5th",
                isTopInning = false,
                countText = "1-2",
                outsText = "2 Outs"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}