package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamRecordDto(
    @SerialName("team")
    val team: TeamDto,

    @SerialName("wins")
    val wins: Int = 0,

    @SerialName("losses")
    val losses: Int = 0,

    @SerialName("winningPercentage")
    val winningPercentage: String? = null,

    @SerialName("divisionRank")
    val divisionRank: String? = null,

    @SerialName("gamesBack")
    val gamesBack: String? = null
)