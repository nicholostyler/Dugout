package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameFeedResponseDto(
    @SerialName("gameData")
    val gameData: GameDataDto? = null,

    @SerialName("liveData")
    val liveData: LiveDataDto? = null
)

@Serializable
data class GameDataDto(
    @SerialName("status")
    val status: StatusDto? = null,

    @SerialName("teams")
    val teams: FeedTeamsDto? = null,

    @SerialName("venue")
    val venue: VenueDto? = null,

    @SerialName("probablePitchers")
    val probablePitchers: ProbablePitchersDto? = null,

    @SerialName("datetime")
    val datetime: DateTimeInfoDto? = null
)

@Serializable
data class StatusDto(
    @SerialName("detailedState")
    val detailedState: String? = null
)

@Serializable
data class DateTimeInfoDto(
    @SerialName("dateTime")
    val dateTime: String? = null,

    @SerialName("officialDate")
    val officialDate: String? = null
)

@Serializable
data class FeedTeamsDto(
    @SerialName("home")
    val home: TeamInfoDto? = null,

    @SerialName("away")
    val away: TeamInfoDto? = null
)

@Serializable
data class TeamInfoDto(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String? = null
)

@Serializable
data class ProbablePitchersDto(
    @SerialName("home")
    val home: PlayerDto? = null,

    @SerialName("away")
    val away: PlayerDto? = null
)

@Serializable
data class PlayerDto(
    @SerialName("id")
    val id: Int,

    @SerialName("fullName")
    val fullName: String? = null
)

@Serializable
data class LiveDataDto(
    @SerialName("linescore")
    val linescore: LinescoreDto? = null,

    @SerialName("plays")
    val plays: PlaysDto? = null
)

@Serializable
data class LinescoreDto(
    @SerialName("currentInning")
    val currentInning: Int = 0,

    @SerialName("inningState")
    val inningState: String? = null,

    @SerialName("teams")
    val teams: TeamsScoreDto? = null
)

@Serializable
data class TeamsScoreDto(
    @SerialName("home")
    val home: TeamRunInfoDto? = null,

    @SerialName("away")
    val away: TeamRunInfoDto? = null
)

@Serializable
data class TeamRunInfoDto(
    @SerialName("runs")
    val runs: Int = 0
)

@Serializable
data class PlaysDto(
    @SerialName("allPlays")
    val allPlays: List<PlayDto> = emptyList()
)

@Serializable
data class PlayDto(
    @SerialName("result")
    val result: ResultDto? = null,

    @SerialName("about")
    val about: AboutDto? = null,

    @SerialName("count")
    val count: CountDto? = null,

    @SerialName("matchup")
    val matchup: MatchupDto? = null
)

@Serializable
data class ResultDto(
    @SerialName("event")
    val event: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("awayScore")
    val awayScore: Int = 0,

    @SerialName("homeScore")
    val homeScore: Int = 0
)

@Serializable
data class AboutDto(
    @SerialName("inning")
    val inning: Int = 0,

    @SerialName("isTopInning")
    val isTopInning: Boolean = false
)

@Serializable
data class CountDto(
    @SerialName("balls")
    val balls: Int = 0,

    @SerialName("strikes")
    val strikes: Int = 0,

    @SerialName("outs")
    val outs: Int = 0
)

@Serializable
data class MatchupDto(
    @SerialName("batter")
    val batter: PlayerDto? = null,

    @SerialName("pitcher")
    val pitcher: PlayerDto? = null
)