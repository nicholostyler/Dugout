package nicholos.tyler.dugout.model.domain


data class GameSummary(
    val gamePk: Int,
    val awayTeam: String? = null,
    val homeTeam: String? = null,
    val awayScore: Int? = null,
    val homeScore: Int? = null,
    val status: String? = null,
    val startTime: String? = null,
    val venue: String? = null
)