package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel

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
