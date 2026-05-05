package nicholos.tyler.dugout.model.domain

import nicholos.tyler.dugout.data.repository.PlayerSplitStatLine

data class PlayerDetails(
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
    val quickStats: List<PlayerQuickStat>,
    val statSections: List<PlayerStatSection>,
    val splitSections: List<PlayerSplitStatLine> = emptyList()
)

data class PlayerQuickStat(
    val label: String,
    val value: String
)

data class PlayerStatSection(
    val category: PlayerStatCategory,
    val season: PlayerStatLine?,
    val career: PlayerStatLine?
)

enum class PlayerStatCategory {
    BATTING,
    PITCHING,
    FIELDING
}

data class PlayerStatLine(
    val primaryStats: List<PlayerStatItem>,
    val secondaryStats: List<PlayerStatItem>
)

data class PlayerStatItem(
    val label: String,
    val value: String
)