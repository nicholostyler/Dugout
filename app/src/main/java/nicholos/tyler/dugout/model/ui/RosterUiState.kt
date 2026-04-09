package nicholos.tyler.dugout.model.ui

data class RosterUiState(
    val isLoading: Boolean = false,
    val sections: List<RosterSectionUiModel> = emptyList(),
    val summary: RosterSummaryUiModel = RosterSummaryUiModel(),
    val error: String? = null
)

data class RosterSummaryUiModel(
    val totalPlayers: Int = 0,
    val pitchers: Int = 0,
    val positionPlayers: Int = 0
)

data class RosterSectionUiModel(
    val title: String,
    val players: List<RosterPlayerUiModel>
)

data class RosterPlayerUiModel(
    val id: Int,
    val name: String,
    val jerseyNumber: String?,
    val position: String,
    val status: String? = null,
    val primaryStatLine: String,
    val isMvp: Boolean = false,
    val secondaryStatLine: String? = null
)