package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.domain.Boxscore
import nicholos.tyler.dugout.model.domain.GameDetails

data class GameDetailUiState(
    val isLoading: Boolean = false,
    val gameDetails: GameDetails? = null,
    val boxscore: Boxscore? = null,
    val error: String? = null
)

class GameDetailViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    private var currentGamePk: Int? = null
    private var lastRefreshDate: LocalDate? = null

    fun loadGame(gamePk: Int, forceRefresh: Boolean = false) {
        if (!forceRefresh && currentGamePk == gamePk && _uiState.value.gameDetails != null) return

        currentGamePk = gamePk

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val gameDetails = repository.getGameDetails(gamePk)
                val boxscore = repository.getBoxscore(gamePk)
                _uiState.value = GameDetailUiState(
                    isLoading = false,
                    gameDetails = gameDetails,
                    boxscore = boxscore
                )
                lastRefreshDate = LocalDate.now()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = t.message ?: "Failed to load game details"
                )
            }
        }
    }

    fun refresh() {
        currentGamePk?.let { loadGame(it, forceRefresh = true) }
    }

    fun refreshIfNeeded() {
        if (shouldRefresh()) {
            currentGamePk?.let { loadGame(it, forceRefresh = true) }
        }
    }

    fun shouldRefresh(today: LocalDate = LocalDate.now()): Boolean {
        val staleFromPreviousDay = lastRefreshDate?.isBefore(today) ?: true
        val liveGame = _uiState.value.gameDetails?.status.isLiveGameStatus()
        return staleFromPreviousDay || liveGame
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
        "final/10",
        "delayed",
        "warmup",
        "pregame"
    ).any { token -> token in status } &&
            "final" !in status &&
            "postponed" !in status &&
            "canceled" !in status &&
            "cancelled" !in status
}
