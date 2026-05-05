package nicholos.tyler.dugout.model.domain

data class Boxscore(
    val away: BoxscoreTeam,
    val home: BoxscoreTeam
)

data class BoxscoreTeam(
    val teamName: String,
    val batters: List<BoxscorePlayer>,
    val pitchers: List<BoxscorePlayer>
)

data class BoxscorePlayer(
    val id: Int,
    val fullName: String,
    val position: String,
    val battingStats: BattingStats? = null,
    val pitchingStats: PitchingStats? = null
)

data class BattingStats(
    val ab: Int,
    val r: Int,
    val h: Int,
    val rbi: Int,
    val bb: Int,
    val k: Int
)

data class PitchingStats(
    val ip: String,
    val h: Int,
    val r: Int,
    val er: Int,
    val bb: Int,
    val k: Int
)
