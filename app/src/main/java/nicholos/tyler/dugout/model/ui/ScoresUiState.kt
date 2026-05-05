package nicholos.tyler.dugout.model.ui

data class ScoresUiState(
    val isLoading: Boolean = true,
    val games: List<GameSnapshotCardUiModel> = emptyList(),
    val error: String? = null
)