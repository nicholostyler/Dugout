package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingsRecordDto(
    @SerialName("division")
    val division: DivisionDto? = null,

    @SerialName("teamRecords")
    val teamRecords: List<TeamRecordDto> = emptyList()
)