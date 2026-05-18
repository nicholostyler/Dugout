package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.domain.TeamDisplayInfo
import nicholos.tyler.dugout.model.ui.GameCardUiModel
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.MvpCategoryUiModel
import nicholos.tyler.dugout.model.ui.TeamMVPsUiModel
import nicholos.tyler.dugout.model.ui.TeamPageUiState
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.components.DivisionStandingUiModel
import nicholos.tyler.dugout.ui.components.DivisionStandingsSection
import nicholos.tyler.dugout.ui.components.GameSnapshotCard
import nicholos.tyler.dugout.ui.components.NoGameTodayCard
import nicholos.tyler.dugout.ui.components.TeamMVPSection
import nicholos.tyler.dugout.ui.components.TenDayStretchSection
import nicholos.tyler.dugout.ui.components.TenDayStretchUiModel
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.TeamPageViewModel

@Composable
fun TeamPageScreen(
    viewModel: TeamPageViewModel,
    teamId: Int,
    modifier: Modifier = Modifier,
    onTodaysGameClick: (Int) -> Unit = {},
    onStretchGameClick: (Int) -> Unit = {},
    onSeasonScheduleClick: () -> Unit = {},
    onTeamRosterClick: () -> Unit = {},
    onViewLeagueClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.loadTeamPage(teamId)
    }

    TeamPageScreenContent(
        uiState = uiState,
        modifier = modifier,
        onTodaysGameClick = onTodaysGameClick,
        onStretchGameClick = onStretchGameClick,
        onSeasonScheduleClick = onSeasonScheduleClick,
        onTeamRosterClick = onTeamRosterClick,
        onViewLeagueClick = onViewLeagueClick
    )
}

@Composable
fun TeamPageScreenContent(
    uiState: TeamPageUiState,
    modifier: Modifier = Modifier,
    onTodaysGameClick: (Int) -> Unit = {},
    onStretchGameClick: (Int) -> Unit = {},
    onSeasonScheduleClick: () -> Unit = {},
    onTeamRosterClick: () -> Unit = {},
    onViewLeagueClick: () -> Unit = {}
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
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    if (uiState.todaysGame != null) {
                        GameSnapshotCard(
                            model = uiState.todaysGame,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onTodaysGameClick
                        )
                    } else {
                        NoGameTodayCard(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                uiState.tenDayStretch?.let { stretch ->
                    item {
                        TenDayStretchSection(
                            model = stretch,
                            onActionClick = onSeasonScheduleClick,
                            onGameClick = { game -> onStretchGameClick(game.id) }
                        )
                    }
                }

                if (uiState.divisionStandings.isNotEmpty()) {
                    item {
                        DivisionStandingsSection(
                            title = uiState.divisionTitle,
                            standings = uiState.divisionStandings,
                            onViewLeagueClick = onViewLeagueClick
                        )
                    }
                }

                uiState.teamMvps?.let { mvps ->
                    item {
                        TeamMVPSection(
                            model = mvps,
                            onViewRosterClick = onTeamRosterClick
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamPageScreenContentPreview() {
    DugoutTheme {
        TeamPageScreenContent(
            uiState = TeamPageUiState(
                isLoading = false,
                todaysGame = GameSnapshotCardUiModel(
                    gameId = 1,
                    leftTeam = TeamScoreUiModel(
                        name = TeamDisplayInfo(1, "Philadelphia Phillies", "Phillies", "PHI"),
                        score = "5"
                    ),
                    rightTeam = TeamScoreUiModel(
                        name = TeamDisplayInfo(2, "Atlanta Braves", "Braves", "ATL"),
                        score = "3"
                    ),
                    status = "Final"
                ),
                tenDayStretch = TenDayStretchUiModel(
                    games = listOf(
                        GameCardUiModel(
                            id = 1,
                            shortDate = "Mar 26",
                            year = "2026",
                            date = "2026-03-26",
                            matchup = "PHI @ ATL",
                            ballpark = "Truist Park",
                            score = "5 - 3",
                            resultText = "W",
                            outcome = GameOutcome.Win
                        ),
                        GameCardUiModel(
                            id = 2,
                            shortDate = "Mar 27",
                            year = "2026",
                            date = "2026-03-27",
                            matchup = "PHI @ ATL",
                            ballpark = "Truist Park",
                            score = "0 - 0",
                            resultText = "",
                            outcome = GameOutcome.Pending
                        )
                    )
                ),
                divisionTitle = "NL East",
                divisionStandings = listOf(
                    DivisionStandingUiModel(1, 1, "PHI", "Phillies", 10, 5, "-"),
                    DivisionStandingUiModel(2, 2, "ATL", "Braves", 9, 6, "1.0"),
                    DivisionStandingUiModel(3, 3, "NYM", "Mets", 8, 7, "2.0")
                ),
                teamMvps = TeamMVPsUiModel(
                    categories = listOf(
                        MvpCategoryUiModel("AVG", ".320", 1, "Bryce Harper"),
                        MvpCategoryUiModel("HR", "12", 2, "Kyle Schwarber"),
                        MvpCategoryUiModel("RBI", "35", 1, "Bryce Harper")
                    )
                )
            )
        )
    }
}
