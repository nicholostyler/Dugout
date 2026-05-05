package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RosterResponseDto(
    @SerialName("roster")
    val roster: List<RosterPlayerDto> = emptyList()
)

@Serializable
data class RosterPlayerDto(
    @SerialName("person")
    val person: PersonDto,
    @SerialName("jerseyNumber")
    val jerseyNumber: String? = null,
    @SerialName("position")
    val position: PositionDto? = null,
    @SerialName("status")
    val status: RosterStatusDto? = null
)

@Serializable
data class PersonDto(
    @SerialName("id")
    val id: Int,
    @SerialName("fullName")
    val fullName: String,
    @SerialName("stats")
    val stats: List<PersonStatWrapperDto> = emptyList()
)

@Serializable
data class PositionDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("abbreviation")
    val abbreviation: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("type")
    val type: String? = null
)

@Serializable
data class RosterStatusDto(
    @SerialName("code")
    val code: String? = null,
    @SerialName("description")
    val description: String? = null
)

@Serializable
data class PersonStatWrapperDto(
    @SerialName("type")
    val type: StatTypeDto? = null,
    @SerialName("group")
    val group: StatGroupDto? = null,
    @SerialName("splits")
    val splits: List<StatSplitDto> = emptyList()
)

@Serializable
data class StatTypeDto(
    @SerialName("displayName")
    val displayName: String? = null
)

@Serializable
data class StatGroupDto(
    @SerialName("displayName")
    val displayName: String? = null
)

@Serializable
data class StatSplitDto(
    @SerialName("stat")
    val stat: PlayerSeasonStatDto? = null
)

@Serializable
data class PlayerSeasonStatDto(
    @SerialName("avg")
    val avg: String? = null,
    @SerialName("homeRuns")
    val homeRuns: Int? = null,
    @SerialName("rbi")
    val rbi: Int? = null,
    @SerialName("ops")
    val ops: String? = null,
    @SerialName("era")
    val era: String? = null,
    @SerialName("strikeOuts")
    val strikeOuts: Int? = null,
    @SerialName("wins")
    val wins: Int? = null,
    @SerialName("whip")
    val whip: String? = null,
    @SerialName("fielding")
    val fielding: String? = null,
    @SerialName("chances")
    val chances: Int? = null,
    @SerialName("putOuts")
    val putOuts: Int? = null,
    @SerialName("errors")
    val errors: Int? = null,
    @SerialName("assists")
    val assists: Int? = null
)