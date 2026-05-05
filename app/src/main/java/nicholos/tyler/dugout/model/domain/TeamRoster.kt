package nicholos.tyler.dugout.model.domain

data class TeamRoster(
    val players: List<RosterPlayer>
)

data class RosterPlayer(
    val id: Int,
    val fullName: String,
    val jerseyNumber: String?,
    val positionAbbreviation: String,
    val positionName: String,
    val positionType: String,
    val status: String?,
    val hitterStats: HitterStats?,
    val pitcherStats: PitcherStats?,
    val fieldingStats: FieldingStats?
)

data class HitterStats(
    val avg: String?,
    val homeRuns: Int?,
    val rbi: Int?,
    val ops: String?
)

data class PitcherStats(
    val era: String?,
    val strikeOuts: Int?,
    val wins: Int?,
    val whip: String?
)

data class FieldingStats(
    val fielding: String?,
    val chances: Int?,
    val putOuts: Int?,
    val errors: Int?,
    val assists: Int?
)