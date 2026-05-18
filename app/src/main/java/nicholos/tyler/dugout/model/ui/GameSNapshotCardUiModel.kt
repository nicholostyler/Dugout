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
    val onFirst: Boolean = false,
    val onSecond: Boolean = false,
    val onThird: Boolean = false,
    val shortDate: String = "",
    val linescore: LinescoreUiModel? = null
) {
    fun isLiveGame(): Boolean {
        val normalized = status.lowercase()
        return normalized.contains("live") ||
                normalized.contains("progress") ||
                normalized.contains("top") ||
                normalized.contains("bot") ||
                normalized.contains("mid") ||
                normalized.contains("end") ||
                inningText.isNotBlank()
    }

    fun isFinalGame(): Boolean {
        val normalized = status.lowercase()
        return normalized.contains("final") ||
                normalized.contains("completed") ||
                normalized.contains("game over")
    }

    fun isUpcomingGame(): Boolean {
        return !isLiveGame() && !isFinalGame()
    }
}

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
