package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable
import nicholos.tyler.dugout.model.domain.GameOutcome

@Immutable
data class GameCardUiModel(
    val id: Int,
    val shortDate: String,
    val year: String,
    val date: String, // Full date for sorting/calendar
    val matchup: String,
    val ballpark: String,
    val score: String,
    val resultText: String = "",
    val outcome: GameOutcome = GameOutcome.Pending,
    val isSelected: Boolean = false,
    val isHome: Boolean = false,
    val opponentAbbreviation: String = "",
    val seriesDescription: String? = null
)

enum class GameCardOutcome {
    Pending,
    Win,
    Loss,
}