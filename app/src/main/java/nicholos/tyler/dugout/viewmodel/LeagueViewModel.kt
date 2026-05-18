package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.LeagueRepository
import nicholos.tyler.dugout.model.ui.LeagueUiState

import java.time.LocalDate

class LeagueViewModel(
    private val leagueRepository: LeagueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueUiState())
    val uiState: StateFlow<LeagueUiState> = _uiState.asStateFlow()

    private var lastRefreshDate: LocalDate? = null

    init {
        loadStandings()
    }

    fun loadStandings(forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.standings != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching {
                leagueRepository.getLeagueStandings()
            }.onSuccess { standings ->
                _uiState.value = LeagueUiState(
                    isLoading = false,
                    standings = standings
                )
                lastRefreshDate = LocalDate.now()
            }.onFailure { throwable ->
                _uiState.value = LeagueUiState(
                    isLoading = false,
                    error = throwable.message ?: "Failed to load standings"
                )
            }
        }
    }

    fun refreshIfNeeded() {
        if (lastRefreshDate?.isBefore(LocalDate.now()) ?: true) {
            loadStandings(forceRefresh = true)
        }
    }
}