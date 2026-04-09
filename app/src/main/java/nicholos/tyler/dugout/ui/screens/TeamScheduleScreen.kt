package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.ui.GameCardUiModel
import nicholos.tyler.dugout.ui.components.GameRowContent
import nicholos.tyler.dugout.viewmodel.TeamScheduleUiState
import nicholos.tyler.dugout.viewmodel.TeamScheduleViewModel

private data class ScheduleSectionUiModel(
    val title: String,
    val games: List<GameCardUiModel>
)

@Composable
fun TeamScheduleScreen(
    viewModel: TeamScheduleViewModel,
    teamId: Int,
    season: Int,
    modifier: Modifier = Modifier,
    onGameClick: (Int) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId, season) {
        viewModel.loadSeasonGames(teamId = teamId, season = season)
    }

    TeamScheduleContent(
        uiState = uiState.value,
        modifier = modifier,
        onGameClick = { gamePk ->
            viewModel.selectGame(gamePk)
            onGameClick(gamePk)
        }
    )
}

@Composable
fun TeamScheduleContent(
    uiState: TeamScheduleUiState,
    modifier: Modifier = Modifier,
    onGameClick: (Int) -> Unit = {}
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        uiState.gameRows.isEmpty() -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No games found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            val sections = uiState.gameRows.toScheduleSections()

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                sections.forEach { section ->
                    stickyHeader {
                        ScheduleSectionHeader(
                            title = section.title
                        )
                    }

                    item {
                        ScheduleSegment(
                            games = section.games,
                            selectedGameId = uiState.selectedGamePk,
                            onGameClick = onGameClick
                        )
                    }
                }
            }
        }
    }
}

private fun List<GameCardUiModel>.toScheduleSections(): List<ScheduleSectionUiModel> {
    return groupBy { game ->
        game.shortDate.take(3)
    }.map { (month, games) ->
        ScheduleSectionUiModel(
            title = month,
            games = games
        )
    }
}

@Composable
private fun ScheduleSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScheduleSegment(
    games: List<GameCardUiModel>,
    selectedGameId: Int?,
    onGameClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        games.forEachIndexed { index, game ->
            val isSelected = game.id == selectedGameId

            ScheduleListItem(
                game = game,
                isSelected = isSelected,
                shape = ListItemDefaults.segmentedShapes(
                    index = index,
                    count = games.size
                ),
                onClick = { onGameClick(game.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScheduleListItem(
    game: GameCardUiModel,
    isSelected: Boolean,
    shape: ListItemShapes,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape.shape,
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        GameRowContent(
            game = game,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamScheduleContentPreview() {
    TeamScheduleContent(
        uiState = TeamScheduleUiState(
            isLoading = false,
            gameRows = listOf(
                GameCardUiModel(
                    id = 1,
                    shortDate = "Apr 4",
                    year = "2026",
                    matchup = "Phillies at Braves",
                    ballpark = "Truist Park",
                    score = "4 - 3",
                    resultText = "W",
                    outcome = GameOutcome.Win,
                    isSelected = false
                ),
                GameCardUiModel(
                    id = 2,
                    shortDate = "Apr 5",
                    year = "2026",
                    matchup = "Phillies at Braves",
                    ballpark = "Truist Park",
                    score = "2 - 6",
                    resultText = "L",
                    outcome = GameOutcome.Loss,
                    isSelected = true
                ),
                GameCardUiModel(
                    id = 3,
                    shortDate = "May 2",
                    year = "2026",
                    matchup = "Phillies at Mets",
                    ballpark = "Citi Field",
                    score = "—",
                    resultText = "",
                    outcome = GameOutcome.Pending,
                    isSelected = false
                )
            ),
            selectedGamePk = 2,
            error = null
        )
    )
}