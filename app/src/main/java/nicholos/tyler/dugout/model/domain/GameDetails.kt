package nicholos.tyler.dugout.model.domain

data class GameDetails(
    val gamePk: Int,
    val awayTeam: String? = null,
    val homeTeam: String? = null,
    val awayScore: Int = 0,
    val homeScore: Int = 0,
    val awayTeamId: Int = 0,
    val homeTeamId: Int = 0,
    val status: String? = null,
    val venue: String? = null,
    val currentInning: Int = 0,
    val inningState: String? = null,
    val awayProbablePitcher: String? = null,
    val homeProbablePitcher: String? = null,
    val lastPlayDescription: String? = null,
    val lastPlayEvent: String? = null,
    val balls: Int = 0,
    val strikes: Int = 0,
    val outs: Int = 0,
    val currentBatter: String? = null,
    val currentPitcher: String? = null,
    val officialDate: String? = null,
    val startDateTime: String? = null,
    val plays: List<PlayItem> = emptyList(),
    val awayHits: Int = 0,
    val homeHits: Int = 0,
    val awayErrors: Int = 0,
    val homeErrors: Int = 0,
    val innings: List<InningScore> = emptyList()
) {
    val inningDisplay: String
        get() = if (currentInning <= 0 || inningState.isNullOrBlank()) "" else "$inningState $currentInning"

    val scoreDisplay: String
        get() = "$awayScore - $homeScore"
}

data class InningScore(
    val number: Int,
    val awayRuns: Int?,
    val homeRuns: Int?
)