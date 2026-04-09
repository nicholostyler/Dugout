package nicholos.tyler.dugout.model.mapper

import nicholos.tyler.dugout.data.api.dto.PersonStatWrapperDto
import nicholos.tyler.dugout.data.api.dto.PlayerSeasonStatDto
import nicholos.tyler.dugout.data.api.dto.RosterPlayerDto
import nicholos.tyler.dugout.data.api.dto.RosterResponseDto
import nicholos.tyler.dugout.model.domain.HitterStats
import nicholos.tyler.dugout.model.domain.PitcherStats
import nicholos.tyler.dugout.model.domain.RosterPlayer
import nicholos.tyler.dugout.model.domain.TeamRoster

fun RosterResponseDto.toTeamRoster(): TeamRoster {
    return TeamRoster(
        players = roster.map { it.toDomain() }
    )
}

private fun RosterPlayerDto.toDomain(): RosterPlayer {
    return RosterPlayer(
        id = person.id,
        fullName = person.fullName,
        jerseyNumber = jerseyNumber,
        positionAbbreviation = position?.abbreviation.orEmpty(),
        positionName = position?.name.orEmpty(),
        positionType = position?.type.orEmpty(),
        status = status?.description,
        hitterStats = person.stats.firstStat("hitting")?.toHitterStats(),
        pitcherStats = person.stats.firstStat("pitching")?.toPitcherStats()
    )
}

private fun PlayerSeasonStatDto.toHitterStats(): HitterStats {
    return HitterStats(
        avg = avg,
        homeRuns = homeRuns,
        rbi = rbi,
        ops = ops
    )
}

private fun PlayerSeasonStatDto.toPitcherStats(): PitcherStats {
    return PitcherStats(
        era = era,
        strikeOuts = strikeOuts,
        wins = wins,
        whip = whip
    )
}

private fun List<PersonStatWrapperDto>.firstStat(group: String): PlayerSeasonStatDto? {
    return firstOrNull {
        it.group?.displayName.equals(group, ignoreCase = true)
    }?.splits?.firstOrNull()?.stat
}