package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerApiResponseDto(
    @SerialName("people")
    val people: List<PlayerApiDto> = emptyList()
)

@Serializable
data class PlayerApiDto(
    @SerialName("id")
    val id: Int,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("primaryNumber")
    val primaryNumber: String? = null,

    @SerialName("currentAge")
    val currentAge: Int? = null,

    @SerialName("height")
    val height: String? = null,

    @SerialName("weight")
    val weight: Int? = null,

    @SerialName("primaryPosition")
    val primaryPosition: PositionDto? = null,

    @SerialName("batSide")
    val batSide: NamedCodeDto? = null,

    @SerialName("pitchHand")
    val pitchHand: NamedCodeDto? = null,

    @SerialName("currentTeam")
    val currentTeam: NamedIdDto? = null,

    @SerialName("stats")
    val stats: List<PlayerHydratedStatGroupDto> = emptyList()
)

@Serializable
data class NamedCodeDto(
    @SerialName("code")
    val code: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("abbreviation")
    val abbreviation: String? = null
)

@Serializable
data class NamedIdDto(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("fullName")
    val fullName: String? = null
)

@Serializable
data class PlayerHydratedStatGroupDto(
    @SerialName("type")
    val type: StatTypeDto? = null,

    @SerialName("group")
    val group: StatTypeDto? = null,

    @SerialName("splits")
    val splits: List<PlayerHydratedStatSplitDto> = emptyList()
)

@Serializable
data class PlayerHydratedStatSplitDto(
    @SerialName("season")
    val season: String? = null,

    @SerialName("stat")
    val stat: PlayerStatValuesDto? = null
)

@Serializable
data class PlayerStatValuesDto(
    @SerialName("avg")
    val avg: String? = null,

    @SerialName("obp")
    val obp: String? = null,

    @SerialName("slg")
    val slg: String? = null,

    @SerialName("ops")
    val ops: String? = null,

    @SerialName("era")
    val era: String? = null,

    @SerialName("whip")
    val whip: String? = null,

    @SerialName("inningsPitched")
    val inningsPitched: String? = null,

    @SerialName("fielding")
    val fielding: String? = null,

    @SerialName("gamesPlayed")
    val gamesPlayed: Int? = null,

    @SerialName("gamesStarted")
    val gamesStarted: Int? = null,

    @SerialName("games")
    val games: Int? = null,

    @SerialName("runs")
    val runs: Int? = null,

    @SerialName("doubles")
    val doubles: Int? = null,

    @SerialName("triples")
    val triples: Int? = null,

    @SerialName("homeRuns")
    val homeRuns: Int? = null,

    @SerialName("strikeOuts")
    val strikeOuts: Int? = null,

    @SerialName("baseOnBalls")
    val baseOnBalls: Int? = null,

    @SerialName("hits")
    val hits: Int? = null,

    @SerialName("atBats")
    val atBats: Int? = null,

    @SerialName("caughtStealing")
    val caughtStealing: Int? = null,

    @SerialName("stolenBases")
    val stolenBases: Int? = null,

    @SerialName("plateAppearances")
    val plateAppearances: Int? = null,

    @SerialName("totalBases")
    val totalBases: Int? = null,

    @SerialName("rbi")
    val rbi: Int? = null,

    @SerialName("leftOnBase")
    val leftOnBase: Int? = null,

    @SerialName("sacBunts")
    val sacBunts: Int? = null,

    @SerialName("sacFlies")
    val sacFlies: Int? = null,

    @SerialName("wins")
    val wins: Int? = null,

    @SerialName("losses")
    val losses: Int? = null,

    @SerialName("saves")
    val saves: Int? = null,

    @SerialName("assists")
    val assists: Int? = null,

    @SerialName("putOuts")
    val putOuts: Int? = null,

    @SerialName("errors")
    val errors: Int? = null,

    @SerialName("chances")
    val chances: Int? = null,

    @SerialName("doublePlays")
    val doublePlays: Int? = null,

    @SerialName("triplePlays")
    val triplePlays: Int? = null,

    @SerialName("throwingErrors")
    val throwingErrors: Int? = null,

    @SerialName("innings")
    val innings: String? = null
)