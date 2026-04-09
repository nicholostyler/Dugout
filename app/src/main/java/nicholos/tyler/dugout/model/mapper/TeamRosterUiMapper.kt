package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.model.domain.RosterPlayer
import nicholos.tyler.dugout.model.domain.TeamRoster
import nicholos.tyler.dugout.model.ui.RosterPlayerUiModel
import nicholos.tyler.dugout.model.ui.RosterSectionUiModel
import nicholos.tyler.dugout.model.ui.RosterSummaryUiModel
import nicholos.tyler.dugout.model.ui.RosterUiState

fun TeamRoster.toRosterUiState(
    mvpPlayerIds: Set<Int> = emptySet()
): RosterUiState {
    val pitchers = players
        .filter { it.isPitcher() }
        .sortedBy { it.fullName }

    val catchers = players
        .filter { it.positionAbbreviation == "C" }
        .sortedBy { it.fullName }

    val infielders = players
        .filter { it.isInfielder() }
        .sortedBy { it.fullName }

    val outfielders = players
        .filter { it.isOutfielder() }
        .sortedBy { it.fullName }

    val others = players
        .filter { it.isOther() }
        .sortedBy { it.fullName }

    val sections = buildList {
        if (pitchers.isNotEmpty()) {
            add(
                RosterSectionUiModel(
                    title = "Pitchers",
                    players = pitchers.map { it.toUiModel(mvpPlayerIds) }
                )
            )
        }

        if (catchers.isNotEmpty()) {
            add(
                RosterSectionUiModel(
                    title = "Catchers",
                    players = catchers.map { it.toUiModel(mvpPlayerIds) }
                )
            )
        }

        if (infielders.isNotEmpty()) {
            add(
                RosterSectionUiModel(
                    title = "Infielders",
                    players = infielders.map { it.toUiModel(mvpPlayerIds) }
                )
            )
        }

        if (outfielders.isNotEmpty()) {
            add(
                RosterSectionUiModel(
                    title = "Outfielders",
                    players = outfielders.map { it.toUiModel(mvpPlayerIds) }
                )
            )
        }

        if (others.isNotEmpty()) {
            add(
                RosterSectionUiModel(
                    title = "Other",
                    players = others.map { it.toUiModel(mvpPlayerIds) }
                )
            )
        }
    }

    return RosterUiState(
        isLoading = false,
        summary = RosterSummaryUiModel(
            totalPlayers = players.size,
            pitchers = pitchers.size,
            positionPlayers = players.size - pitchers.size
        ),
        sections = sections
    )
}

private fun RosterPlayer.toUiModel(
    mvpPlayerIds: Set<Int>
): RosterPlayerUiModel {
    val primaryStatLine = if (isPitcher()) {
        buildPitcherPrimaryLine()
    } else {
        buildHitterPrimaryLine()
    }

    val secondaryStatLine = if (isPitcher()) {
        buildPitcherSecondaryLine()
    } else {
        buildHitterSecondaryLine()
    }

    return RosterPlayerUiModel(
        id = id,
        name = fullName,
        jerseyNumber = jerseyNumber,
        position = positionAbbreviation.ifBlank { positionName },
        status = status?.takeIf {
            it.isNotBlank() && !it.equals("Active", ignoreCase = true)
        },
        primaryStatLine = primaryStatLine,
        isMvp = id in mvpPlayerIds,
        secondaryStatLine = secondaryStatLine
    )
}

private fun RosterPlayer.buildHitterPrimaryLine(): String {
    val avgText = hitterStats?.avg?.let { "AVG $it" } ?: "AVG -"
    val hrText = hitterStats?.homeRuns?.let { "HR $it" } ?: "HR -"
    val rbiText = hitterStats?.rbi?.let { "RBI $it" } ?: "RBI -"
    val opsText = hitterStats?.ops?.let { "OPS $it" } ?: "OPS -"

    return listOf(avgText, hrText, rbiText, opsText).joinToString(" • ")
}

private fun RosterPlayer.buildHitterSecondaryLine(): String? {
    return null
}

private fun RosterPlayer.buildPitcherPrimaryLine(): String {
    val eraText = pitcherStats?.era?.let { "ERA $it" } ?: "ERA -"
    val soText = pitcherStats?.strikeOuts?.let { "SO $it" } ?: "SO -"

    return listOf(eraText, soText).joinToString(" • ")
}

private fun RosterPlayer.buildPitcherSecondaryLine(): String? {
    val parts = buildList {
        pitcherStats?.wins?.let { add("W $it") }
        pitcherStats?.whip?.let { add("WHIP $it") }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ")
}

private fun RosterPlayer.isPitcher(): Boolean {
    return positionType.equals("Pitcher", ignoreCase = true) || positionAbbreviation == "P"
}

private fun RosterPlayer.isInfielder(): Boolean {
    return positionAbbreviation in setOf("1B", "2B", "3B", "SS")
}

private fun RosterPlayer.isOutfielder(): Boolean {
    return positionAbbreviation in setOf("LF", "CF", "RF", "OF")
}

private fun RosterPlayer.isOther(): Boolean {
    return !isPitcher() &&
            positionAbbreviation != "C" &&
            !isInfielder() &&
            !isOutfielder()
}