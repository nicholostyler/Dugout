package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable

@Immutable
data class GameSnapshotCardUiModel(
    val gameId: Int,
    val leftTeam: TeamScoreUiModel,
    val rightTeam: TeamScoreUiModel,
    val status: String,
    val inningText: String = "",
    val isTopInning: Boolean? = null,
    val countText: String = "",
    val outsText: String = "",
)