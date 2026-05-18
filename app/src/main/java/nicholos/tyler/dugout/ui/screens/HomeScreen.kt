package nicholos.tyler.dugout.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.ui.GameCardUiModel
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.HomeUiState
import nicholos.tyler.dugout.model.ui.MvpCategoryUiModel
import nicholos.tyler.dugout.model.ui.TeamMVPsUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.components.DivisionStandingUiModel
import nicholos.tyler.dugout.ui.components.DivisionStandingsSection
import nicholos.tyler.dugout.ui.components.GameSnapshotCard
import nicholos.tyler.dugout.ui.components.NoGameTodayCard
import nicholos.tyler.dugout.ui.components.TeamMVPSection
import nicholos.tyler.dugout.ui.components.TenDayStretchSection
import nicholos.tyler.dugout.ui.components.TenDayStretchUiModel
import nicholos.tyler.dugout.ui.theme.DugoutTheme
import nicholos.tyler.dugout.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    teamId: Int,
    modifier: Modifier = Modifier,
    onTodaysGameClick: (Int) -> Unit = {},
    onStretchGameClick: (Int) -> Unit = {},
    onSeasonScheduleClick: () -> Unit = {},
    onTeamRosterClick: () -> Unit = {},
    onViewLeagueClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.loadHome(teamId)
    }

    HomeScreenContent(
        uiState = uiState,
        modifier = modifier,
        onTodaysGameClick = onTodaysGameClick,
        onStretchGameClick = onStretchGameClick,
        onSeasonScheduleClick = onSeasonScheduleClick,
        onTeamRosterClick = onTeamRosterClick,
        onViewLeagueClick = onViewLeagueClick,
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onTodaysGameClick: (Int) -> Unit = {},
    onStretchGameClick: (Int) -> Unit = {},
    onSeasonScheduleClick: () -> Unit = {},
    onTeamRosterClick: () -> Unit = {},
    onViewLeagueClick: () -> Unit = {},
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
                    .fillMaxSize(),
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
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
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
                            onGameClick = { game ->
                                onStretchGameClick(game.id)
                            }
                        )
                    }

                }

                if (uiState.divisionStandings.isNotEmpty()) {
                    item {
                        DivisionStandingsSection(
                            title = uiState.divisionTitle,
                            standings = uiState.divisionStandings,
                            onViewLeagueClick = onViewLeagueClick,
                            modifier = Modifier.fillMaxWidth()
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
private fun HomeScreenPreview() {
    DugoutTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                todaysGame = GameSnapshotCardUiModel(
                    gameId = 1,
                    leftTeam = TeamScoreUiModel(MlbTeams.get(143), "5"),
                    rightTeam = TeamScoreUiModel(MlbTeams.get(116), "3"),
                    status = "Live",


                ),
                tenDayStretch = TenDayStretchUiModel(
                    games = listOf(
                        GameCardUiModel(
                            id = 1,
                            shortDate = "Mar 26",
                            year = "2026",
                            date = "2026-03-26",
                            matchup = "Phillies @ Braves",
                            ballpark = "Truist Park",
                            score = "5 - 3",
                            resultText = "Win",
                            outcome = GameOutcome.Win,
                        ),
                        GameCardUiModel(
                            id = 2,
                            shortDate = "Mar 27",
                            year = "2026",
                            date = "2026-03-27",
                            matchup = "Phillies @ Mets",
                            ballpark = "Citi Field",
                            score = "—",
                            resultText = "",
                            outcome = GameOutcome.Pending,
                        )
                    )
                ),
                divisionTitle = "NL East",
                divisionStandings = listOf(
                    DivisionStandingUiModel(1, 143, "PHI", "Phillies", 10, 5, "-"),
                    DivisionStandingUiModel(2, 144, "ATL", "Braves", 9, 6, "1.0"),
                    DivisionStandingUiModel(3, 121, "NYM", "Mets", 8, 7, "2.0")
                ),
                teamMvps = TeamMVPsUiModel(
                    categories = listOf(
                        MvpCategoryUiModel(
                            label = "AVG",
                            value = ".311",
                            playerId = 134,
                            playerName = "Tea Turner"
                        ),
                        MvpCategoryUiModel(
                            label = "HR",
                            value = "32",
                            playerId = 143,
                            playerName = "Kyle Schwarber"
                        ),
                        MvpCategoryUiModel(
                            label = "RBI",
                            value = "98",
                            playerId = 143,
                            playerName = "Kyle Schwarber"
                        ),
                        MvpCategoryUiModel(
                            label = "ERA",
                            value = "2.84",
                            playerId = 143,
                            playerName = "Christopher Sanchez"
                        ),
                        MvpCategoryUiModel(
                            label = "SO",
                            value = "187",
                            playerId = 143,
                            playerName = "Zack Wheeler"
                        )
                    )
            )
            ))
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
                        value = ".311",
                        playerId = 134,
                        playerName = "Tea Turner"
                    ),
                    MvpCategoryUiModel(
                        label = "HR",
                        value = "32",
                        playerId = 143,
                        playerName = "Kyle Schwarber"
                    ),
                    MvpCategoryUiModel(
                        label = "RBI",
                        value = "98",
                        playerId = 143,
                        playerName = "Kyle Schwarber"
                    ),
                    MvpCategoryUiModel(
                        label = "ERA",
                        value = "2.84",
                        playerId = 143,
                        playerName = "Christopher Sanchez"
                    ),
                    MvpCategoryUiModel(
                        label = "SO",
                        value = "187",
                        playerId = 143,
                        playerName = "Zack Wheeler"
                    )
                )
            ),
            onViewRosterClick = {}
        )
    }
}
