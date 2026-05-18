package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.mapper.toScoresSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.ScoresUiState

import java.time.LocalDate

class ScoresViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoresUiState())
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    private var lastRefreshDate: LocalDate? = null

    init {
        loadScores(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadScores(date)
    }

    fun loadScores(date: LocalDate, forceRefresh: Boolean = false) {
        if (!forceRefresh && lastRefreshDate == date && _uiState.value.games.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                repository.getGamesByDate(date)
                    .map { it.toScoresSnapshotCardUiModel() }
            }.onSuccess { games ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        games = games,
                        selectedDate = date
                    )
                }
                lastRefreshDate = date
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load scores"
                    )
                }
            }
        }
    }

    fun refreshIfNeeded() {
        if (shouldRefresh()) {
            loadScores(_uiState.value.selectedDate, forceRefresh = true)
        }
    }

    private fun shouldRefresh(): Boolean {
        val selectedDate = _uiState.value.selectedDate
        val isToday = selectedDate == LocalDate.now()
        val hasLiveGames = _uiState.value.games.any { it.status.isLiveGameStatus() }
        return isToday || hasLiveGames
    }
}

private fun String?.isLiveGameStatus(): Boolean {
    if (this.isNullOrBlank()) return false
    val status = this.lowercase()
    return listOf("live", "in progress", "mid", "top", "bottom", "inning", "delayed", "warmup", "pregame")
        .any { it in status } && "final" !in status && "postponed" !in status
}