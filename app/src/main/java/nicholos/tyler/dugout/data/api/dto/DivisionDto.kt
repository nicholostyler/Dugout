package nicholos.tyler.dugout.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DivisionDto(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("link")
    val link: String? = null
)