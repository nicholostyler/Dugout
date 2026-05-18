package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleResponseDto(
    @SerialName("dates")
    val dates: List<GameDateDto> = emptyList()
)

@Serializable
data class GameDateDto(
    @SerialName("date")
    val date: String? = null,

    @SerialName("games")
    val games: List<GameDto> = emptyList()
)

@Serializable
data class GameDto(
    @SerialName("gamePk")
    val gamePk: Int,

    @SerialName("gameGuid")
    val gameGuid: String? = null,

    @SerialName("gameDate")
    val gameDate: String? = null,

    @SerialName("gameType")
    val gameType: String? = null,

    @SerialName("status")
    val status: GameStatusDto? = null,

    @SerialName("teams")
    val teams: GameTeamsDto? = null,

    @SerialName("venue")
    val venue: VenueDto? = null,

    @SerialName("scheduledInnings")
    val scheduledInnings: Int? = null,

    @SerialName("seriesDescription")
    val seriesDescription: String? = null,

    @SerialName("linescore")
    val linescore: LinescoreDto? = null
)

@Serializable
data class GameStatusDto(
    @SerialName("abstractGameState")
    val abstractGameState: String? = null,

    @SerialName("detailedState")
    val detailedState: String? = null,

    @SerialName("statusCode")
    val statusCode: String? = null
)

@Serializable
data class GameTeamsDto(
    @SerialName("home")
    val home: TeamSideDto? = null,

    @SerialName("away")
    val away: TeamSideDto? = null
)

@Serializable
data class TeamSideDto(
    @SerialName("team")
    val team: TeamDto? = null,

    @SerialName("leagueRecord")
    val leagueRecord: LeagueRecordDto? = null,

    @SerialName("score")
    val score: Int? = null,

    @SerialName("probablePitcher")
    val probablePitcher: ProbablePitcherDto? = null
)

@Serializable
data class ProbablePitcherDto(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("fullName")
    val fullName: String? = null
)

@Serializable
data class TeamDto(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String? = null
)

@Serializable
data class LeagueRecordDto(
    @SerialName("wins")
    val wins: Int? = null,

    @SerialName("losses")
    val losses: Int? = null,

    @SerialName("pct")
    val pct: String? = null
)

@Serializable
data class VenueDto(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String? = null
)