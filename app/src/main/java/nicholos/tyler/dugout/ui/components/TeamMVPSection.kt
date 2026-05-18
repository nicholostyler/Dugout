package nicholos.tyler.dugout.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nicholos.tyler.dugout.model.ui.MvpCategoryUiModel
import nicholos.tyler.dugout.model.ui.TeamMVPsUiModel
import nicholos.tyler.dugout.ui.theme.DugoutTheme

@Composable
fun TeamMVPSection(
    model: TeamMVPsUiModel,
    onViewRosterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Team MVPs",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Team Roster",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onViewRosterClick)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(model.categories) { category ->
                MVPStatCard(model = category)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamMVPSectionPreview() {
    DugoutTheme {
        TeamMVPSection(
            model = TeamMVPsUiModel(
                categories = listOf(
                    MvpCategoryUiModel(
                        label = "AVG",
                        value = ".324",
                        playerId = 1,
                        playerName = "Ronald Acuña Jr."
                    ),
                    MvpCategoryUiModel(
                        label = "HR",
                        value = "41",
                        playerId = 2,
                        playerName = "Matt Olson"
                    ),
                    MvpCategoryUiModel(
                        label = "RBI",
                        value = "139",
                        playerId = 3,
                        playerName = "Matt Olson"
                    ),
                    MvpCategoryUiModel(
                        label = "OPS",
                        value = "1.012",
                        playerId = 4,
                        playerName = "Ronald Acuña Jr."
                    )
                )
            ),
            onViewRosterClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun MVPStatCard(
    model: MvpCategoryUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(148.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = model.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = model.value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = model.playerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}