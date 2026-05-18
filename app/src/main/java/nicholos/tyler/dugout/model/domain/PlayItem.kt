package nicholos.tyler.dugout.model.domain

data class PlayItem(
    val description: String? = null,
    val event: String? = null,
    val inning: Int = 0,
    val isTopInning: Boolean = false,
    val batterName: String? = null,
    val pitcherName: String? = null,
    val batterId: Int? = null,
    val pitches: List<PitchItem> = emptyList()
) {
    val inningDisplay: String
        get() = "${if (isTopInning) "Top" else "Bottom"} $inning"
}

data class PitchItem(
    val result: String? = null,        // e.g. "Ball", "Called Strike"
    val pitchType: String? = null,     // e.g. "Four-Seam Fastball"
    val velocity: Double? = null,      // speed in mph
    val px: Float? = null,             // horizontal coordinate
    val pz: Float? = null,             // vertical coordinate
    val balls: Int? = null,            // balls after pitch
    val strikes: Int? = null           // strikes after pitch
)
