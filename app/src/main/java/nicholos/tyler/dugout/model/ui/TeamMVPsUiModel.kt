package nicholos.tyler.dugout.model.ui

data class TeamMVPsUiModel(
    val categories: List<MvpCategoryUiModel>
)

data class MvpCategoryUiModel(
    val label: String,
    val value: String,
    val playerId: Int,
    val playerName: String
)