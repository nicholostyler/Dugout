package nicholos.tyler.dugout.model.domain


data class Game(
    val gamePk: Int,
    val gameGuid: String? = null,
    val gameDate: String? = null,
    val gameType: String? = null,
    val status: GameStatus? = null,
    val teams: GameTeams? = null,
    val venue: Venue? = null,
    val scheduledInnings: Int? = null,
    val seriesDescription: String? = null,
    val linescore: Linescore? = null
)

data class Linescore(
    val currentInning: Int? = null,
    val inningState: String? = null,
    val inningHalf: String? = null,
    val isTopInning: Boolean? = null,
    val scheduledInnings: Int? = null,
    val runs: Int? = null,
    val hits: Int? = null,
    val errors: Int? = null,
    val balls: Int? = null,
    val strikes: Int? = null,
    val outs: Int? = null,
    val onFirst: Boolean = false,
    val onSecond: Boolean = false,
    val onThird: Boolean = false
)

data class GameStatus(
    val abstractGameState: String? = null,
    val detailedState: String? = null,
    val statusCode: String? = null
)

data class GameTeams(
    val home: TeamSide? = null,
    val away: TeamSide? = null
)

data class TeamSide(
    val team: Team? = null,
    val leagueRecord: LeagueRecord? = null,
    val score: Int? = null,
    val probablePitcher: ProbablePitcher? = null
)

data class ProbablePitcher(
    val id: Int? = null,
    val fullName: String? = null
)

data class Team(
    val id: Int,
    val name: String? = null
)

data class LeagueRecord(
    val wins: Int? = null,
    val losses: Int? = null,
    val pct: String? = null
)

data class Venue(
    val id: Int,
    val name: String? = null
)
