package nicholos.tyler.dugout.model.domain

data class CachedGame(
    val gamePk: Int = 0,
    val gameDate: String = "",
    val status: String = "",
    val abstractGameState: String = "",
    val awayTeamId: Int = 0,
    val awayTeamName: String = "",
    val awayScore: Int = 0,
    val homeTeamId: Int = 0,
    val homeTeamName: String = "",
    val homeScore: Int = 0
)