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
import nicholos.tyler.dugout.model.ui.HomeUiState
import nicholos.tyler.dugout.ui.screens.toSnapshotUiModel

class HomeViewModel(
    private val repository: GamesRepository,
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadedTeamId: Int? = null
    private var lastRefreshDate: LocalDate? = null

    fun loadHome(teamId: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh && loadedTeamId == teamId && hasContent()) {
            return
        }

        viewModelScope.launch {
            val refreshExistingContent = forceRefresh && hasContent()
            _uiState.value = _uiState.value.copy(
                isLoading = !refreshExistingContent,
                isRefreshing = refreshExistingContent,
                error = null
            )

            try {
                val todaysScheduledGame = repository.getTodaysGame(teamId)

                val stretchGamesDeferred = async {
                    repository.getStretchGames(teamId)
                }

                val teamMvpsDeferred = async {
                    repository.getTeamMVPs(teamId)
                }

                val divisionDeferred = async {
                    leagueRepository.getDivisionStandingsForTeam(teamId)
                }

                val todaysGameDetails = todaysScheduledGame?.let { scheduledGame ->
                    repository.getGameDetails(scheduledGame.gamePk)
                }

                val todaysSnapshotCard = todaysGameDetails?.toSnapshotUiModel()
                val stretchGames = stretchGamesDeferred.await()
                val teamMvps = teamMvpsDeferred.await()
                val division = divisionDeferred.await()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    isRefreshing = false,
                    todaysGame = todaysSnapshotCard,
                    tenDayStretch = stretchGames.toTenDayStretchUiModel(teamId),
                    teamMvps = teamMvps.toUiModel(),
                    divisionTitle = division?.divisionName?.toShortDivisionTitle() ?: "",
                    divisionStandings = division?.toUiModels(selectedTeamId = teamId).orEmpty()
                )

                loadedTeamId = teamId
                lastRefreshDate = LocalDate.now()
            } catch (t: Throwable) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    isRefreshing = false,
                    error = t.message ?: "Failed to load home"
                )
            }
        }
    }

    fun refresh(teamId: Int) {
        loadHome(teamId, forceRefresh = true)
    }

    fun refreshIfNeeded(teamId: Int) {
        if (shouldRefresh()) {
            loadHome(teamId, forceRefresh = true)
        }
    }

    fun shouldRefresh(today: LocalDate = LocalDate.now()): Boolean {
        val staleFromPreviousDay = lastRefreshDate?.isBefore(today) ?: true
        val liveGame = _uiState.value.todaysGame?.status.isLiveGameStatus()
        return staleFromPreviousDay || liveGame
    }

    private fun hasContent(): Boolean {
        val state = _uiState.value
        return state.todaysGame != null ||
                state.tenDayStretch != null ||
                state.teamMvps != null
    }
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
        "final/10", // optional if your backend ever formats oddly
        "delayed",
        "warmup",
        "pregame"
    ).any { token -> token in status } &&
            "final" !in status &&
            "postponed" !in status &&
            "canceled" !in status &&
            "cancelled" !in status
}
