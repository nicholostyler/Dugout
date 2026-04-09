package nicholos.tyler.dugout.model.domain

data class TeamMVPs(
    val battingAverageLeader: StatLeader? = null,
    val homeRunLeader: StatLeader? = null,
    val rbiLeader: StatLeader? = null,
    val eraLeader: StatLeader? = null,
    val strikeoutLeader: StatLeader? = null
)

data class StatLeader(
    val playerId: Int,
    val playerName: String,
    val statLabel: String,
    val statValue: String
)