package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.mapper.toRosterUiState
import nicholos.tyler.dugout.model.ui.RosterUiState

class RosterViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RosterUiState())
    val uiState: StateFlow<RosterUiState> = _uiState.asStateFlow()

    private var loadedTeamId: Int? = null

    fun loadRoster(teamId: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh && loadedTeamId == teamId && _uiState.value.sections.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val rosterDeferred = async { repository.getTeamRoster(teamId) }
                val mvpsDeferred = async { repository.getTeamMVPs(teamId) }

                val roster = rosterDeferred.await()
                val mvps = mvpsDeferred.await()

                val mvpPlayerIds = buildSet {
                    mvps.battingAverageLeader?.playerId?.let(::add)
                    mvps.homeRunLeader?.playerId?.let(::add)
                    mvps.rbiLeader?.playerId?.let(::add)
                    mvps.eraLeader?.playerId?.let(::add)
                    mvps.strikeoutLeader?.playerId?.let(::add)
                }

                loadedTeamId = teamId
                _uiState.value = roster.toRosterUiState(mvpPlayerIds = mvpPlayerIds)
            } catch (t: Throwable) {
                _uiState.value = RosterUiState(
                    isLoading = false,
                    error = t.message ?: "Failed to load roster"
                )
            }
        }
    }
}