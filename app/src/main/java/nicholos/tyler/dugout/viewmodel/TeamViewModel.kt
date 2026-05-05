package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.data.repository.LeagueRepository
import nicholos.tyler.dugout.model.mapper.toTenDayStretchUiModel
import nicholos.tyler.dugout.model.mapper.toUiModel
import nicholos.tyler.dugout.model.mapper.toUiModels
import nicholos.tyler.dugout.model.ui.TeamPageUiState
import nicholos.tyler.dugout.ui.screens.toSnapshotUiModel

class TeamPageViewModel(
    private val gamesRepository: GamesRepository,
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamPageUiState())
    val uiState: StateFlow<TeamPageUiState> = _uiState.asStateFlow()

    private var loadedTeamId: Int? = null
    private var lastRefreshDate: LocalDate? = null

    fun loadTeamPage(teamId: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh && loadedTeamId == teamId) {
            return
        }

        viewModelScope.launch {
            _uiState.value = TeamPageUiState(isLoading = true)
            loadedTeamId = teamId

            try {
                val todaysScheduledGame = gamesRepository.getTodaysGame(teamId)

                val stretchGamesDeferred = async {
                    gamesRepository.getStretchGames(teamId)
                }

                val teamMvpsDeferred = async {
                    gamesRepository.getTeamMVPs(teamId)
                }

                val divisionDeferred = async {
                    leagueRepository.getDivisionStandingsForTeam(teamId)
                }

                val todaysGameDetails = todaysScheduledGame?.let { scheduledGame ->
                    gamesRepository.getGameDetails(scheduledGame.gamePk)
                }

                val todaysSnapshotCard = todaysGameDetails?.toSnapshotUiModel()
                val stretchGames = stretchGamesDeferred.await()
                val teamMvps = teamMvpsDeferred.await()
                val division = divisionDeferred.await()

                _uiState.value = TeamPageUiState(
                    isLoading = false,
                    todaysGame = todaysSnapshotCard,
                    tenDayStretch = stretchGames.toTenDayStretchUiModel(teamId),
                    teamMvps = teamMvps.toUiModel(),
                    divisionTitle = division?.divisionName?.toShortDivisionTitle() ?: "",
                    divisionStandings = division?.toUiModels(selectedTeamId = teamId).orEmpty()
                )

                lastRefreshDate = LocalDate.now()
            } catch (t: Throwable) {
                _uiState.value = TeamPageUiState(
                    isLoading = false,
                    error = t.message ?: "Failed to load team page"
                )
            }
        }
    }

    fun refreshIfNeeded(teamId: Int) {
        if (shouldRefresh()) {
            loadTeamPage(teamId, forceRefresh = true)
        }
    }

    fun shouldRefresh(today: LocalDate = LocalDate.now()): Boolean {
        val staleFromPreviousDay = lastRefreshDate?.isBefore(today) ?: true
        val liveGame = _uiState.value.todaysGame?.status.isLiveGameStatus()
        return staleFromPreviousDay || liveGame
    }
}

fun String.toShortDivisionTitle(): String {
    return this
        .replace("American League", "AL")
        .replace("National League", "NL")
}

private fun String?.isLiveGameStatus(): Boolean {
    if (this.isNullOrBlank()) return false

    val status = this.lowercase()

    return listOf(
        "live",
        "in progress",
        "mid",
        "top",
        "bottom",
        "inning",
        "delayed",
        "warmup",
        "pregame"
    ).any { token -> token in status } &&
            "final" !in status &&
            "postponed" !in status &&
            "canceled" !in status &&
            "cancelled" !in status
}