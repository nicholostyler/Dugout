package nicholos.tyler.dugout.model.ui

import androidx.compose.runtime.Immutable
import nicholos.tyler.dugout.model.domain.GameOutcome

@Immutable
data class GameCardUiModel(
    val id: Int,
    val shortDate: String,
    val year: String,
    val matchup: String,
    val ballpark: String,
    val score: String,
    val resultText: String = "",
    val outcome: GameOutcome = GameOutcome.Pending,
    val isSelected: Boolean = false
)

enum class GameCardOutcome {
    Pending,
    Win,
    Loss,
}