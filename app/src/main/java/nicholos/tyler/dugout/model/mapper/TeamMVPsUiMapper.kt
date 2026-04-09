package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.model.domain.StatLeader
import nicholos.tyler.dugout.model.domain.TeamMVPs
import nicholos.tyler.dugout.model.ui.MvpCategoryUiModel
import nicholos.tyler.dugout.model.ui.TeamMVPsUiModel

fun TeamMVPs.toUiModel(): TeamMVPsUiModel {
    return TeamMVPsUiModel(
        categories = buildList {
            battingAverageLeader?.let { add(it.toUiCategory()) }
            homeRunLeader?.let { add(it.toUiCategory()) }
            rbiLeader?.let { add(it.toUiCategory()) }
            eraLeader?.let { add(it.toUiCategory()) }
            strikeoutLeader?.let { add(it.toUiCategory()) }
        }
    )
}

private fun StatLeader.toUiCategory(): MvpCategoryUiModel {
    return MvpCategoryUiModel(
        label = statLabel,
        value = statValue,
        playerId = playerId,
        playerName = playerName
    )
}