package nicholos.tyler.dugout.model.ui

import nicholos.tyler.dugout.model.domain.PlayerStatCategory
import nicholos.tyler.dugout.model.domain.PlayerStatItem

data class PlayerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val player: PlayerProfileUiModel? = null,
    val selectedCategory: PlayerStatCategory = PlayerStatCategory.BATTING,
    val selectedRange: PlayerStatRange = PlayerStatRange.SEASON
)

enum class PlayerStatRange {
    SEASON,
    CAREER
}

data class PlayerProfileUiModel(
    val id: Int,
    val fullName: String,
    val jerseyNumber: String?,
    val position: String,
    val teamName: String?,
    val age: Int?,
    val height: String?,
    val weight: Int?,
    val bats: String?,
    val throwsHand: String?,
    val quickStats: List<PlayerQuickStatUiModel>,
    val categories: List<PlayerCategoryStatsUiModel>,
    val splits: List<PlayerSplitStatsUiModel>
)

data class PlayerQuickStatUiModel(
    val label: String,
    val value: String
)

data class PlayerCategoryStatsUiModel(
    val category: PlayerStatCategory,
    val seasonPrimary: List<PlayerStatItem>,
    val seasonSecondary: List<PlayerStatItem>,
    val careerPrimary: List<PlayerStatItem>,
    val careerSecondary: List<PlayerStatItem>
)

data class PlayerSplitStatsUiModel(
    val title: String,
    val stats: List<PlayerStatItem>
)