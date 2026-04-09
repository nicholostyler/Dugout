package nicholos.tyler.dugout.model.domain

data class PlayItem(
    val description: String? = null,
    val event: String? = null,
    val inning: Int = 0,
    val isTopInning: Boolean = false,
    val batterName: String? = null,
    val pitcherName: String? = null,
    val batterId: Int? = null,
) {
    val inningDisplay: String
        get() = "${if (isTopInning) "Top" else "Bottom"} $inning"
}