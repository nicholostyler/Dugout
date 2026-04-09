package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.mapper.ballpark
import nicholos.tyler.dugout.model.mapper.matchup
import nicholos.tyler.dugout.model.mapper.outcomeFor
import nicholos.tyler.dugout.model.mapper.resultFor
import nicholos.tyler.dugout.model.mapper.scoreDisplay
import nicholos.tyler.dugout.model.mapper.shortDate
import nicholos.tyler.dugout.model.mapper.year
import nicholos.tyler.dugout.model.ui.GameCardUiModel

data class TeamScheduleUiState(
    val isLoading: Boolean = false,
    val gameRows: List<GameCardUiModel> = emptyList(),
    val selectedGamePk: Int? = null,
    val error: String? = null
)

class TeamScheduleViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamScheduleUiState())
    val uiState: StateFlow<TeamScheduleUiState> = _uiState.asStateFlow()

    private var currentGames: List<Game> = emptyList()
    private var currentTeamId: Int? = null
    private var currentSeason: Int? = null

    fun loadSeasonGames(teamId: Int, season: Int) {
        if (
            currentTeamId == teamId &&
            currentSeason == season &&
            currentGames.isNotEmpty()
        ) return

        currentTeamId = teamId
        currentSeason = season

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val games = repository.getSeasonGames(teamId, season)
                currentGames = games

                val selectedGamePk = _uiState.value.selectedGamePk

                val rows = withContext(Dispatchers.Default) {
                    buildGameRows(
                        games = games,
                        teamId = teamId,
                        selectedGamePk = selectedGamePk
                    )
                }

                _uiState.value = TeamScheduleUiState(
                    isLoading = false,
                    gameRows = rows,
                    selectedGamePk = selectedGamePk,
                    error = null
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = t.message ?: "Failed to load schedule"
                )
            }
        }
    }

    fun selectGame(gamePk: Int) {
        val teamId = currentTeamId ?: return

        viewModelScope.launch {
            val rows = withContext(Dispatchers.Default) {
                buildGameRows(
                    games = currentGames,
                    teamId = teamId,
                    selectedGamePk = gamePk
                )
            }

            _uiState.value = _uiState.value.copy(
                selectedGamePk = gamePk,
                gameRows = rows
            )
        }
    }

    private fun buildGameRows(
        games: List<Game>,
        teamId: Int,
        selectedGamePk: Int?
    ): List<GameCardUiModel> {
        return games.map { game ->
            game.toGameCardUiModel(
                teamId = teamId,
                selectedGamePk = selectedGamePk
            )
        }
    }
}

private fun Game.toGameCardUiModel(
    teamId: Int,
    selectedGamePk: Int?
): GameCardUiModel {
    return GameCardUiModel(
        id = gamePk,
        shortDate = shortDate(),
        year = year(),
        matchup = matchup(),
        ballpark = ballpark(),
        score = scoreDisplay(),
        resultText = resultFor(teamId),
        outcome = outcomeFor(teamId),
        isSelected = selectedGamePk == gamePk
    )
}