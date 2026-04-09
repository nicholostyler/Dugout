package nicholos.tyler.dugout.model.domain

data class LeagueStandings(
    val divisions: List<DivisionStandings>
)

data class DivisionStandings(
    val divisionName: String,
    val teams: List<DivisionTeamStanding>
)

data class DivisionTeamStanding(
    val rank: Int,
    val teamId: Int,
    val teamName: String,
    val wins: Int,
    val losses: Int,
    val winPct: Float,
    val gamesBack: String
)