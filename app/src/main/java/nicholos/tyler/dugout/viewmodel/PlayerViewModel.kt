package nicholos.tyler.dugout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nicholos.tyler.dugout.data.repository.GamesRepository
import nicholos.tyler.dugout.model.domain.PlayerDetails
import nicholos.tyler.dugout.model.domain.PlayerStatCategory
import nicholos.tyler.dugout.model.ui.PlayerCategoryStatsUiModel
import nicholos.tyler.dugout.model.ui.PlayerProfileUiModel
import nicholos.tyler.dugout.model.ui.PlayerQuickStatUiModel
import nicholos.tyler.dugout.model.ui.PlayerSplitStatsUiModel
import nicholos.tyler.dugout.model.ui.PlayerStatRange
import nicholos.tyler.dugout.model.ui.PlayerUiState

import java.time.LocalDate

class PlayerViewModel(
    private val repository: GamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState(isLoading = true))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var lastPlayerId: Int? = null
    private var lastRefreshDate: LocalDate? = null

    fun loadPlayer(playerId: Int, season: Int = 2026, forceRefresh: Boolean = false) {
        if (!forceRefresh && lastPlayerId == playerId && _uiState.value.player != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching<PlayerDetails> {
                repository.getPlayerDetails(playerId, season)
            }.onSuccess { player ->
                val playerUiModel = player.toUiModel()

                _uiState.value = PlayerUiState(
                    isLoading = false,
                    player = playerUiModel,
                    selectedCategory = resolveDefaultCategory(playerUiModel),
                    selectedRange = PlayerStatRange.SEASON
                )
                lastPlayerId = playerId
                lastRefreshDate = LocalDate.now()
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Unable to load player"
                )
            }
        }
    }

    fun refreshIfNeeded() {
        if (lastRefreshDate?.isBefore(LocalDate.now()) ?: true) {
            lastPlayerId?.let { loadPlayer(it, forceRefresh = true) }
        }
    }

    fun onCategorySelected(category: PlayerStatCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onRangeSelected(range: PlayerStatRange) {
        _uiState.update { it.copy(selectedRange = range) }
    }

    private fun resolveDefaultCategory(player: PlayerProfileUiModel): PlayerStatCategory {
        val available = player.categories.map { it.category }

        val preferred = when {
            player.position.equals("P", ignoreCase = true) -> PlayerStatCategory.PITCHING
            player.position.contains("pitcher", ignoreCase = true) -> PlayerStatCategory.PITCHING
            else -> PlayerStatCategory.BATTING
        }

        return when {
            preferred in available -> preferred
            PlayerStatCategory.BATTING in available -> PlayerStatCategory.BATTING
            PlayerStatCategory.PITCHING in available -> PlayerStatCategory.PITCHING
            PlayerStatCategory.FIELDING in available -> PlayerStatCategory.FIELDING
            else -> preferred
        }
    }
}

private fun PlayerDetails.toUiModel(): PlayerProfileUiModel {
    return PlayerProfileUiModel(
        id = id,
        fullName = fullName,
        jerseyNumber = jerseyNumber,
        position = position,
        teamName = teamName,
        age = age,
        height = height,
        weight = weight,
        bats = bats,
        throwsHand = throwsHand,
        quickStats = quickStats.map {
            PlayerQuickStatUiModel(
                label = it.label,
                value = it.value
            )
        },
        categories = statSections.map { section ->
            PlayerCategoryStatsUiModel(
                category = section.category,
                seasonPrimary = section.season?.primaryStats.orEmpty(),
                seasonSecondary = section.season?.secondaryStats.orEmpty(),
                careerPrimary = section.career?.primaryStats.orEmpty(),
                careerSecondary = section.career?.secondaryStats.orEmpty()
            )
        },
        splits = splitSections.map { split ->
            PlayerSplitStatsUiModel(
                title = split.title,
                stats = split.stats
            )
        }
    )
}