package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingsResponseDto(
    @SerialName("records")
    val records: List<StandingsRecordDto> = emptyList()
)