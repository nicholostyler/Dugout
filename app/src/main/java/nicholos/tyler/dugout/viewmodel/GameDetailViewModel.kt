package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadGame(gamePk: Int) {
        if (currentGamePk == gamePk && _uiState.value.gameDetails != null) return

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
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = t.message ?: "Failed to load game details"
                )
            }
        }
    }
}