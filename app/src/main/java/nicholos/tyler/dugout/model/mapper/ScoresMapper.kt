package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.model.domain.CachedGame
import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Game.toScoresSnapshotCardUiModel(): GameSnapshotCardUiModel {
    val away = teams?.away?.team
    val home = teams?.home?.team
    val detailedState = status?.detailedState.orEmpty()

    return GameSnapshotCardUiModel(
        gameId = gamePk,
        leftTeam = TeamScoreUiModel(
            name = MlbTeams.get(away?.id ?: 0, fallbackName = away?.name),
            score = teams?.away?.score?.toString() ?: "—",
            record = teams?.away?.leagueRecord?.let { "${it.wins}-${it.losses}" } ?: "",
            probablePitcher = teams?.away?.probablePitcher?.fullName
        ),
        rightTeam = TeamScoreUiModel(
            name = MlbTeams.get(home?.id ?: 0, fallbackName = home?.name),
            score = teams?.home?.score?.toString() ?: "—",
            record = teams?.home?.leagueRecord?.let { "${it.wins}-${it.losses}" } ?: "",
            probablePitcher = teams?.home?.probablePitcher?.fullName
        ),
        status = detailedState,
        startTime = startTime(),
        inningText = when {
            detailedState.contains("Top", ignoreCase = true) -> detailedState
            detailedState.contains("Bot", ignoreCase = true) -> detailedState
            detailedState.contains("Middle", ignoreCase = true) -> detailedState
            detailedState.contains("End", ignoreCase = true) -> detailedState
            else -> ""
        },
        isTopInning = when {
            detailedState.startsWith("Top", ignoreCase = true) -> true
            detailedState.startsWith("Bot", ignoreCase = true) -> false
            else -> linescore?.isTopInning
        },
        countText = linescore?.let { "${it.balls}-${it.strikes}" } ?: "",
        outsText = linescore?.outs?.let { "$it Out${if (it != 1) "s" else ""}" } ?: "",
        onFirst = linescore?.onFirst ?: false,
        onSecond = linescore?.onSecond ?: false,
        onThird = linescore?.onThird ?: false,
        shortDate = shortDate()
    )
}

fun CachedGame.toScoresSnapshotCardUiModel(): GameSnapshotCardUiModel {
    val isLiveGame = isLiveGame()
    val isFinalGame = isFinalGame()
    val startTime = startTime()
    val displayStatus = when {
        isLiveGame || isFinalGame -> status.ifBlank { abstractGameState }
        shouldDisplayStartTime() && startTime.isNotBlank() -> startTime
        else -> status.ifBlank { abstractGameState }
    }

    return GameSnapshotCardUiModel(
        gameId = gamePk,
        leftTeam = TeamScoreUiModel(
            name = MlbTeams.get(awayTeamId, fallbackName = awayTeamName),
            score = if (isLiveGame || isFinalGame) awayScore.toString() else "",
            record = ""
        ),
        rightTeam = TeamScoreUiModel(
            name = MlbTeams.get(homeTeamId, fallbackName = homeTeamName),
            score = if (isLiveGame || isFinalGame) homeScore.toString() else "",
            record = ""
        ),
        status = displayStatus,
        startTime = startTime,
        inningText = status.takeIf { it.isInningState() }.orEmpty(),
        isTopInning = when {
            status.startsWith("Top", ignoreCase = true) -> true
            status.startsWith("Bot", ignoreCase = true) -> false
            else -> null
        },
        shortDate = shortDate(),
        linescore = null
    )
}

private fun CachedGame.isLiveGame(): Boolean {
    val normalizedStatus = status.lowercase(Locale.US)
    val normalizedAbstractState = abstractGameState.lowercase(Locale.US)

    if (isFinalGame()) return false

    return normalizedAbstractState == "live" ||
            listOf("live", "progress", "top", "bot", "mid", "end", "warmup", "pregame", "delayed")
                .any { it in normalizedStatus }
}

private fun CachedGame.isFinalGame(): Boolean {
    val normalizedStatus = status.lowercase(Locale.US)
    val normalizedAbstractState = abstractGameState.lowercase(Locale.US)

    return normalizedAbstractState == "final" ||
            normalizedStatus.contains("final") ||
            normalizedStatus.contains("completed") ||
            normalizedStatus.contains("game over")
}

private fun CachedGame.startTime(): String {
    return try {
        OffsetDateTime.parse(gameDate)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    } catch (_: Exception) {
        ""
    }
}

private fun CachedGame.shouldDisplayStartTime(): Boolean {
    val normalizedStatus = status.lowercase(Locale.US)
    val normalizedAbstractState = abstractGameState.lowercase(Locale.US)

    return normalizedAbstractState == "preview" ||
            normalizedStatus in setOf("scheduled", "pre-game", "pregame", "warmup")
}

private fun CachedGame.shortDate(): String {
    return try {
        OffsetDateTime.parse(gameDate)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM dd", Locale.US))
    } catch (_: Exception) {
        ""
    }
}

private fun String.isInningState(): Boolean {
    return contains("Top", ignoreCase = true) ||
            contains("Bot", ignoreCase = true) ||
            contains("Middle", ignoreCase = true) ||
            contains("End", ignoreCase = true)
}
