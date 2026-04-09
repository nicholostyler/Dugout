package nicholos.tyler.dugout.model.domain

data class StretchGame(
    val shortDate: String = "",
    val year: String = "",
    val matchup: String = "",
    val ballpark: String = "",
    val score: String = "",
    val result: String = "",
    val isLoss: Boolean = false
)