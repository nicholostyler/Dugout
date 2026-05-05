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

class ScoresViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoresUiState())
    val uiState: StateFlow<ScoresUiState> = _uiState.asStateFlow()

    init {
        loadScores()
    }

    fun loadScores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                repository.getTodaysGames()
                    .map { it.toScoresSnapshotCardUiModel() }
            }.onSuccess { games ->
                _uiState.value = ScoresUiState(
                    isLoading = false,
                    games = games
                )
            }.onFailure { throwable ->
                _uiState.value = ScoresUiState(
                    isLoading = false,
                    error = throwable.message ?: "Failed to load scores"
                )
            }
        }
    }
}