package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.mapper.toGameSnapshotCardUiModel
import nicholos.tyler.dugout.model.mapper.toTenDayStretchUiModel
import nicholos.tyler.dugout.model.mapper.toUiModel
import nicholos.tyler.dugout.model.ui.HomeUiState
import nicholos.tyler.dugout.ui.screens.toSnapshotUiModel

class HomeViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadHome(teamId: Int, forceRefresh: Boolean = false) {
        if (
            !forceRefresh &&
            (
                    _uiState.value.todaysGame != null ||
                            _uiState.value.tenDayStretch != null ||
                            _uiState.value.teamMvps != null
                    )
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
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

                val todaysGameDetails = todaysScheduledGame?.let { scheduledGame ->
                    repository.getGameDetails(scheduledGame.gamePk)
                }

                val todaysSnapshotCard = todaysGameDetails?.toSnapshotUiModel()
                val stretchGames = stretchGamesDeferred.await()
                val teamMvps = teamMvpsDeferred.await()

                _uiState.value = HomeUiState(
                    isLoading = false,
                    todaysGame = todaysSnapshotCard,
                    tenDayStretch = stretchGames.toTenDayStretchUiModel(teamId),
                    teamMvps = teamMvps.toUiModel()
                )
            } catch (t: Throwable) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = t.message ?: "Failed to load home"
                )
            }
        }
    }
}