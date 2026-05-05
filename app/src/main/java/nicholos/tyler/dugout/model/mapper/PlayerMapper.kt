package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.PlayerStatValuesDto
import nicholos.tyler.dugout.model.domain.PlayerStatItem
import nicholos.tyler.dugout.model.domain.PlayerStatLine

fun PlayerStatValuesDto.toBattingLine(): PlayerStatLine {
    return PlayerStatLine(
        primaryStats = listOf(
            PlayerStatItem("AVG", avg.display()),
            PlayerStatItem("OPS", ops.display()),
            PlayerStatItem("HR", homeRuns.display()),
            PlayerStatItem("RBI", rbi.display())
        ),
        secondaryStats = listOf(
            PlayerStatItem("G", gamesPlayed.display()),
            PlayerStatItem("R", runs.display()),
            PlayerStatItem("H", hits.display()),
            PlayerStatItem("TB", totalBases.display()),
            PlayerStatItem("2B", doubles.display()),
            PlayerStatItem("3B", triples.display()),
            PlayerStatItem("BB", baseOnBalls.display()),
            PlayerStatItem("SO", strikeOuts.display()),
            PlayerStatItem("SB", stolenBases.display())
        )
    )
}

fun PlayerStatValuesDto.toPitchingLine(): PlayerStatLine {
    return PlayerStatLine(
        primaryStats = listOf(
            PlayerStatItem("ERA", era.display()),
            PlayerStatItem("WHIP", whip.display()),
            PlayerStatItem("W", wins.display()),
            PlayerStatItem("L", losses.display())
        ),
        secondaryStats = listOf(
            PlayerStatItem("IP", inningsPitched.display()),
            PlayerStatItem("SO", strikeOuts.display()),
            PlayerStatItem("BB", baseOnBalls.display()),
            PlayerStatItem("SV", saves.display())
        )
    )
}

fun PlayerStatValuesDto.toPitchingSplitItems(): List<PlayerStatItem> {
    return listOf(
        PlayerStatItem("ERA", era.display()),
        PlayerStatItem("WHIP", whip.display()),
        PlayerStatItem("IP", inningsPitched.display()),
        PlayerStatItem("SO", strikeOuts.display()),
        PlayerStatItem("BB", baseOnBalls.display()),
        PlayerStatItem("W", wins.display()),
        PlayerStatItem("L", losses.display()),
        PlayerStatItem("SV", saves.display())
    )
}

fun PlayerStatValuesDto.toBattingSplitItems(): List<PlayerStatItem> {
    return listOf(
        PlayerStatItem("AVG", avg.display()),
        PlayerStatItem("OPS", ops.display()),
        PlayerStatItem("G", gamesPlayed.display()),
        PlayerStatItem("H", hits.display()),
        PlayerStatItem("HR", homeRuns.display()),
        PlayerStatItem("RBI", rbi.display()),
        PlayerStatItem("BB", baseOnBalls.display()),
        PlayerStatItem("SO", strikeOuts.display())
    )
}

private fun Any?.display(): String = this?.toString() ?: "--"