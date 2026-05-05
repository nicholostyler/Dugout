package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable

@Immutable
data class GameSnapshotCardUiModel(
    val gameId: Int,
    val leftTeam: TeamScoreUiModel,
    val rightTeam: TeamScoreUiModel,
    val status: String,
    val startTime: String = "",
    val inningText: String = "",
    val isTopInning: Boolean? = null,
    val countText: String = "",
    val outsText: String = "",
    val shortDate: String = "",
    val linescore: LinescoreUiModel? = null
)

@Immutable
data class LinescoreUiModel(
    val innings: List<InningScoreUiModel>,
    val leftTotal: TeamTotalUiModel,
    val rightTotal: TeamTotalUiModel
)

@Immutable
data class InningScoreUiModel(
    val number: Int,
    val leftRuns: String,
    val rightRuns: String
)

@Immutable
data class TeamTotalUiModel(
    val runs: Int,
    val hits: Int,
    val errors: Int
)
