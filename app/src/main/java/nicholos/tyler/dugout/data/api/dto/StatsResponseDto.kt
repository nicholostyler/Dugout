package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsResponseDto(
    @SerialName("stats")
    val stats: List<StatsGroupDto> = emptyList(),
    
    @SerialName("leagueLeaders")
    val leagueLeaders: List<StatsGroupDto> = emptyList()
)

@Serializable
data class StatsGroupDto(
    @SerialName("type")
    val type: StatTypeDto? = null,

    @SerialName("group")
    val group: StatTypeDto? = null,

    @SerialName("leaderCategory")
    val leaderCategory: String? = null,

    @SerialName("splits")
    val splits: List<StatsSplitDto> = emptyList(),

    @SerialName("leaders")
    val leaders: List<StatsSplitDto> = emptyList()
)

@Serializable
data class StatsSplitDto(
    @SerialName("season")
    val season: String? = null,

    @SerialName("stat")
    val stat: PlayerStatValuesDto? = null,

    @SerialName("team")
    val team: NamedIdDto? = null,

    @SerialName("player")
    val player: NamedIdDto? = null,

    @SerialName("person")
    val person: NamedIdDto? = null,

    @SerialName("league")
    val league: NamedIdDto? = null,

    @SerialName("sport")
    val sport: NamedIdDto? = null,

    @SerialName("value")
    val value: String? = null,

    @SerialName("rank")
    val rank: Int? = null
)
