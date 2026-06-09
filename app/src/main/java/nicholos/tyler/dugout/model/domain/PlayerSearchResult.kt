package nicholos.tyler.dugout.model.domain

data class PlayerSearchResult(
    val id: Int,
    val fullName: String,
    val jerseyNumber: String?,
    val position: String?,
    val teamId: Int?,
    val teamName: String?,
    val bats: String?,
    val throwsHand: String?,
    val active: Boolean
)
