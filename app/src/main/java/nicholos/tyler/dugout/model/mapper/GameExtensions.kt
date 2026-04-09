package nicholos.tyler.dugout.model.mapper


import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.GameOutcome
import nicholos.tyler.dugout.model.domain.MlbTeams
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Game.shortDate(): String {
    return try {
        val date = gameDate ?: return ""
        val parsed = OffsetDateTime.parse(date)
        parsed.format(DateTimeFormatter.ofPattern("MMM dd", Locale.US))
    } catch (_: Exception) {
        ""
    }
}

fun Game.year(): String {
    return try {
        val date = gameDate ?: return ""
        val parsed = OffsetDateTime.parse(date)
        parsed.year.toString()
    } catch (_: Exception) {
        ""
    }
}

fun Game.matchup(): String {
    val away = MlbTeams.get(teams?.away?.team?.id ?: 0, fallbackName = teams?.away?.team?.name)
    val home = MlbTeams.get(teams?.home?.team?.id ?: 0, fallbackName = teams?.home?.team?.name)
    return "${away.abbreviation} @ ${home.abbreviation}"
}

fun Game.ballpark(): String {
    return venue?.name ?: ""
}

fun Game.scoreDisplay(): String {
    val away = teams?.away?.score
    val home = teams?.home?.score

    return if (away == null || home == null) {
        "—"
    } else {
        "$away - $home"
    }
}

fun Game.resultFor(teamId: Int): String {
    val awayId = teams?.away?.team?.id
    val homeId = teams?.home?.team?.id

    val awayScore = teams?.away?.score
    val homeScore = teams?.home?.score

    if (awayScore == null || homeScore == null) return ""

    return when {
        awayId == teamId -> if (awayScore > homeScore) "Win" else "Loss"
        homeId == teamId -> if (homeScore > awayScore) "Win" else "Loss"
        else -> ""
    }
}

fun Game.outcomeFor(teamId: Int): GameOutcome {
    val awayId = teams?.away?.team?.id
    val homeId = teams?.home?.team?.id

    val awayScore = teams?.away?.score
    val homeScore = teams?.home?.score

    if (awayScore == null || homeScore == null) return GameOutcome.Pending

    return when {
        awayId == teamId -> if (awayScore > homeScore) GameOutcome.Win else GameOutcome.Loss
        homeId == teamId -> if (homeScore > awayScore) GameOutcome.Win else GameOutcome.Loss
        else -> GameOutcome.Pending
    }
}