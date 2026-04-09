package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.LeagueRepository
import nicholos.tyler.dugout.model.ui.LeagueUiState

class LeagueViewModel(
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueUiState())
    val uiState: StateFlow<LeagueUiState> = _uiState.asStateFlow()

    init {
        loadStandings()
    }

    fun loadStandings() {
        viewModelScope.launch {
            _uiState.value = LeagueUiState(isLoading = true)

            runCatching {
                leagueRepository.getLeagueStandings()
            }.onSuccess { standings ->
                _uiState.value = LeagueUiState(
                    isLoading = false,
                    standings = standings
                )
            }.onFailure { throwable ->
                _uiState.value = LeagueUiState(
                    isLoading = false,
                    error = throwable.message ?: "Failed to load standings"
                )
            }
        }
    }
}