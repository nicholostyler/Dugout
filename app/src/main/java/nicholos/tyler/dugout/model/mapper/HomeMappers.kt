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
            score = teams?.away?.score?.toString() ?: "—"
        ),
        rightTeam = TeamScoreUiModel(
            name = MlbTeams.get(home?.id ?: 0, fallbackName = home?.name),
            score = teams?.home?.score?.toString() ?: "—"
        ),
        status = status?.detailedState.orEmpty(),
        countText = "",
        outsText = "",
        inningText = "",
        isTopInning = null,
    )
}

fun List<Game>.toTenDayStretchUiModel(teamId: Int): TenDayStretchUiModel {
    return TenDayStretchUiModel(
        games = map { game ->
            GameCardUiModel(
                id = game.gamePk,
                shortDate = game.shortDate(),
                year = game.year(),
                matchup = game.matchup(),
                ballpark = game.ballpark(),
                score = game.scoreDisplay(),
                resultText = game.resultFor(teamId),
                outcome = game.outcomeFor(teamId),
                isSelected = false,

            )
        }
    )
}