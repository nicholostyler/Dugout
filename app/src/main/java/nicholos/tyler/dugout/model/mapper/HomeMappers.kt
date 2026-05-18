package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.model.domain.Game
import nicholos.tyler.dugout.model.domain.MlbTeams
import nicholos.tyler.dugout.model.ui.GameCardUiModel
import nicholos.tyler.dugout.model.ui.GameSnapshotCardUiModel
import nicholos.tyler.dugout.model.ui.TeamScoreUiModel
import nicholos.tyler.dugout.ui.components.TenDayStretchUiModel

fun Game.toGameSnapshotCardUiModel(): GameSnapshotCardUiModel {
    val away = teams?.away?.team
    val home = teams?.home?.team

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
        status = status?.detailedState.orEmpty(),
        startTime = startTime(),
        countText = "",
        outsText = "",
        inningText = "",
        isTopInning = null,
        shortDate = shortDate()
    )
}

fun List<Game>.toTenDayStretchUiModel(teamId: Int): TenDayStretchUiModel {
    return TenDayStretchUiModel(
        games = map { game ->
            val isHome = game.teams?.home?.team?.id == teamId
            val opponent = if (isHome) game.teams?.away?.team else game.teams?.home?.team
            val opponentId = opponent?.id ?: 0
            val opponentAbbr = MlbTeams.byId[opponentId]?.abbreviation ?: opponent?.name ?: ""

            GameCardUiModel(
                id = game.gamePk,
                shortDate = game.shortDate(),
                year = game.year(),
                date = game.gameDate ?: "",
                matchup = game.matchup(),
                ballpark = game.ballpark(),
                score = game.scoreDisplay(),
                resultText = game.resultFor(teamId),
                outcome = game.outcomeFor(teamId),
                isSelected = false,
                isHome = isHome,
                opponentAbbreviation = opponentAbbr,
                seriesDescription = game.seriesDescription
            )
        }
    )
}