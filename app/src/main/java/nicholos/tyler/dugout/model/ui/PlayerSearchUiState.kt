package nicholos.tyler.dugout.model.ui

data class PlayerSearchUiState(
    val query: String = "",
    val season: Int,
    val players: List<PlayerSearchResultUiModel> = emptyList(),
    val isSyncing: Boolean = false,
    val hasLoadedCache: Boolean = false,
    val error: String? = null
)

data class PlayerSearchResultUiModel(
    val id: Int,
    val fullName: String,
    val jerseyNumber: String?,
    val position: String,
    val teamId: Int?,
    val teamName: String?,
    val handedness: String?,
    val active: Boolean
)
