package nicholos.tyler.dugout.model.ui

import java.time.LocalDate

data class ScoresUiState(
    val isLoading: Boolean = true,
    val games: List<GameSnapshotCardUiModel> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val error: String? = null
)