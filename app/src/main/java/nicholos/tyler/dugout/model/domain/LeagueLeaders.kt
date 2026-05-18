package nicholos.tyler.dugout.model.domain

data class LeagueLeaderGroup(
    val categoryId: String,
    val categoryName: String,
    val leaders: List<LeagueLeader>
)

data class LeagueLeader(
    val rank: Int,
    val id: Int,
    val name: String,
    val teamName: String?,
    val statValue: String,
    val isPlayer: Boolean
)