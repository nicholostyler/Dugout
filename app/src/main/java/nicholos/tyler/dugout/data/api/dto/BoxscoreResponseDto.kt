package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoxscoreResponseDto(
    @SerialName("teams")
    val teams: BoxscoreTeamsDto? = null
)

@Serializable
data class BoxscoreTeamsDto(
    @SerialName("away")
    val away: BoxscoreTeamDto? = null,
    @SerialName("home")
    val home: BoxscoreTeamDto? = null
)

@Serializable
data class BoxscoreTeamDto(
    @SerialName("team")
    val team: TeamInfoDto? = null,
    @SerialName("players")
    val players: Map<String, BoxscorePlayerDto> = emptyMap(),
    @SerialName("batters")
    val batters: List<Int> = emptyList(),
    @SerialName("pitchers")
    val pitchers: List<Int> = emptyList(),
    @SerialName("bench")
    val bench: List<Int> = emptyList(),
    @SerialName("bullpen")
    val bullpen: List<Int> = emptyList()
)

@Serializable
data class BoxscorePlayerDto(
    @SerialName("person")
    val person: PlayerDto? = null,
    @SerialName("stats")
    val stats: BoxscorePlayerStatsDto? = null,
    @SerialName("position")
    val position: PositionDto? = null
)

@Serializable
data class BoxscorePlayerStatsDto(
    @SerialName("batting")
    val batting: BoxscoreBattingStatsDto? = null,
    @SerialName("pitching")
    val pitching: BoxscorePitchingStatsDto? = null
)

@Serializable
data class BoxscoreBattingStatsDto(
    @SerialName("runs") val runs: Int = 0,
    @SerialName("hits") val hits: Int = 0,
    @SerialName("rbi") val rbi: Int = 0,
    @SerialName("homeRuns") val homeRuns: Int = 0,
    @SerialName("strikeOuts") val strikeOuts: Int = 0,
    @SerialName("baseOnBalls") val baseOnBalls: Int = 0,
    @SerialName("atBats") val atBats: Int = 0
)

@Serializable
data class BoxscorePitchingStatsDto(
    @SerialName("inningsPitched") val inningsPitched: String? = null,
    @SerialName("hits") val hits: Int = 0,
    @SerialName("runs") val runs: Int = 0,
    @SerialName("earnedRuns") val earnedRuns: Int = 0,
    @SerialName("baseOnBalls") val baseOnBalls: Int = 0,
    @SerialName("strikeOuts") val strikeOuts: Int = 0,
    @SerialName("homeRuns") val homeRuns: Int = 0
)
